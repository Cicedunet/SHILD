package com.example.shielmind.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.shielmind.ai.TFLiteClassifier
import com.example.shielmind.service.EmailSender
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ShieldAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "ShieldMind_Service"

        // Common Android browser packages to analyze
        private val BROWSER_PACKAGES = setOf(
            "com.android.chrome",
            "org.mozilla.firefox",
            "com.microsoft.emmx",
            "com.opera.browser",
            "com.sec.android.app.sbrowser",
            "com.duckduckgo.mobile.android",
            "com.brave.browser",
            "com.android.browser",
            "org.mozilla.focus"
        )
    }

    private val throttler = CaptureThrottler()
    private lateinit var classifier: TFLiteClassifier

    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private var screenTimeRunnable: Runnable? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "ShieldMind Accessibility Service démarré avec succès")
        classifier = TFLiteClassifier(this)

        // Start screen time background tracker
        screenTimeRunnable = object : Runnable {
            override fun run() {
                try {
                    checkAndUpdateScreenTime()
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking screen time: ${e.message}")
                }
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(screenTimeRunnable!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        screenTimeRunnable?.let { handler.removeCallbacks(it) }
        if (::classifier.isInitialized) {
            classifier.close()
        }
        Log.d(TAG, "Service ShieldMind arrêté proprement")
    }

    private fun checkAndUpdateScreenTime() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        val isInteractive = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT_WATCH) {
            powerManager.isInteractive
        } else {
            @Suppress("DEPRECATION")
            powerManager.isScreenOn
        }

        if (!isInteractive) return

        val prefs = getSharedPreferences("shieldmind_prefs", Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val savedDate = prefs.getString("screen_time_today_date", "") ?: ""

        var usedSeconds = prefs.getInt("screen_time_used_seconds", 0)

        if (today != savedDate) {
            usedSeconds = 0
            prefs.edit()
                .putString("screen_time_today_date", today)
                .putInt("screen_time_used_seconds", 0)
                .apply()
        } else {
            usedSeconds += 1
            prefs.edit().putInt("screen_time_used_seconds", usedSeconds).apply()
        }

        val limitMinutes = prefs.getInt("screen_time_limit_minutes", 30) // default 30 mins
        if (usedSeconds >= limitMinutes * 60) {
            val intent = android.content.Intent(this, com.example.shielmind.MainActivity::class.java).apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra("BLOCK_REASON", "TEMPS_EPUISE")
            }
            startActivity(intent)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val packageName = event.packageName?.toString() ?: "inconnu"

        // Enforce Screen Time limit if exceeded, but allow interaction with our app (com.example.shielmind)
        val timePrefs = getSharedPreferences("shieldmind_prefs", Context.MODE_PRIVATE)
        val usedSeconds = timePrefs.getInt("screen_time_used_seconds", 0)
        val limitMinutes = timePrefs.getInt("screen_time_limit_minutes", 30)
        if (usedSeconds >= limitMinutes * 60) {
            if (packageName != "com.example.shielmind") {
                val intent = android.content.Intent(this, com.example.shielmind.MainActivity::class.java).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    putExtra("BLOCK_REASON", "TEMPS_EPUISE")
                }
                startActivity(intent)
                return
            }
        }

        // ANALYSE UNIQUEMENT DANS LES NAVIGATEURS
        if (!BROWSER_PACKAGES.contains(packageName)) {
            return
        }

        val rootNode: AccessibilityNodeInfo? = event.source ?: rootInActiveWindow
        if (rootNode == null) return

        val rawText = extractAllText(rootNode)

        if (rawText.isBlank()) return

        val cleanedText = TextNoiseFilter.clean(rawText)

        if (!TextNoiseFilter.isWorthAnalyzing(cleanedText)) return

        if (!throttler.shouldAnalyze(cleanedText)) return

        val content = CapturedContent.create(
            text = cleanedText,
            sourceApp = packageName
        )

        Log.d(TAG, "Contenu prêt pour analyse : app=${content.sourceApp}, " +
                "${content.characterCount} caractères, " +
                "texte=\"${content.text.take(100)}...\"")

        // DÉTECTION IA UNIQUEMENT (TFLite)
        val toxicityScore = classifier.classify(content.text)

        Log.d(TAG, "Score de toxicité final : $toxicityScore")

        // Récupération de l'âge de l'enfant pour adapter le seuil de sensibilité
        val prefs = getSharedPreferences("shieldmind_prefs", Context.MODE_PRIVATE)
        val ageProfile = prefs.getString("age_profile", "enfant") ?: "enfant"

        // Seuil dynamique de blocage selon le profil d'âge
        val threshold = when (ageProfile) {
            "adulte" -> 0.85f       // Adulte : Moins strict, bloque seulement la forte toxicité
            "ado" -> 0.60f          // Adolescent : Équilibré
            else -> 0.35f           // Enfant : Très strict, bloque au moindre doute
        }

        if (toxicityScore > threshold) {
            Log.w(TAG, "CONTENU TOXIQUE DÉTECTÉ ($ageProfile) ! Blocage en cours... (Score: $toxicityScore, Seuil: $threshold)")

            // Ferme uniquement l'onglet/page active du navigateur en effectuant un retour
            performGlobalAction(GLOBAL_ACTION_BACK)

            // Enregistrer l'événement de blocage dans l'historique local pour l'afficher sur le tableau de bord
            saveBlockEventToHistory(content, toxicityScore)

            // Envoi de l'alerte par mail au parent
            sendAlertToParentByEmail(content, ageProfile, toxicityScore)
        }
    }

    private fun saveBlockEventToHistory(content: CapturedContent, score: Float) {
        val prefs = getSharedPreferences("shieldmind_prefs", Context.MODE_PRIVATE)
        val historySet = prefs.getStringSet("block_history", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

        val timestamp = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        val appName = content.sourceApp.substringAfterLast(".")
        val textSnippet = content.text.take(100).replace("|", " ").replace("\n", " ")

        val eventString = "$timestamp|$appName|$textSnippet|${String.format(Locale.US, "%.2f", score)}"

        historySet.add(eventString)
        prefs.edit().putStringSet("block_history", historySet).apply()
    }

    private fun sendAlertToParentByEmail(content: CapturedContent, ageProfile: String, score: Float) {
        val prefs = getSharedPreferences("shieldmind_prefs", Context.MODE_PRIVATE)
        val childEmail = prefs.getString("child_email", "") ?: ""
        val parentEmail = prefs.getString("parent_email", "") ?: ""

        if (parentEmail.isBlank()) {
            Log.e(TAG, "Impossible d'envoyer le mail : email parent non configuré.")
            return
        }

        val subject = "[ShieldMind] Alerte : Contenu inapproprié bloqué"
        val bodyText = """
            Bonjour,

            Une alerte de contenu inapproprié a été détectée et bloquée sur l'appareil de votre enfant ($childEmail).

            Détails de l'alerte :
            - Profil d'âge : $ageProfile
            - Score de toxicité : ${String.format(Locale.US, "%.2f", score)}
            - Application : ${content.sourceApp}
            - Texte détecté :
            ${content.text}

            La page contenant ce contenu inapproprié a été fermée immédiatement sur l'appareil de votre enfant.

            Cordialement,
            L'équipe ShieldMind
        """.trimIndent()

        EmailSender.sendEmail(
            context = this,
            recipientEmail = parentEmail,
            subject = subject,
            bodyText = bodyText,
            onSuccess = {
                Log.i(TAG, "Mail d'alerte envoyé avec succès au parent : $parentEmail")
            },
            onFailure = { e ->
                Log.e(TAG, "Échec de l'envoi du mail d'alerte au parent : ${e.message}")
            }
        )
    }

    private fun extractAllText(node: AccessibilityNodeInfo): String {
        val builder = StringBuilder()

        val nodeText = node.text?.toString()
        if (!nodeText.isNullOrBlank()) {
            builder.append(nodeText).append(" ")
        }

        val contentDescription = node.contentDescription?.toString()
        if (!contentDescription.isNullOrBlank()) {
            builder.append(contentDescription).append(" ")
        }

        for (i in 0 until node.childCount) {
            val childNode = node.getChild(i)
            if (childNode != null) {
                builder.append(extractAllText(childNode))
            }
        }

        return builder.toString()
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service interrompu")
    }
}
