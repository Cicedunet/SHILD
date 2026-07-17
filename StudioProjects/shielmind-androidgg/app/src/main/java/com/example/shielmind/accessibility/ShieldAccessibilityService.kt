package com.example.shielmind.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.shielmind.ai.TFLiteClassifier
import com.example.shielmind.service.EmailSender

class ShieldAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "ShieldMind_Service"

        // Common Android browser packages to analyze
        private val BROWSER_PACKAGES = setOf(
            "com.android.chrome",
            "org.mozilla.firefox",
            "com.microsoft.emmx",
            "com.opera.browser",
            "com.opera.mini.native",
            "com.sec.android.app.sbrowser",
            "com.duckduckgo.mobile.android",
            "com.brave.browser",
            "com.android.browser",
            "org.mozilla.focus",
            "com.huawei.browser",
            "com.yandex.browser",
            "com.UCMobile.intl",
            "mark.via.gp"
        )
    }

    private val throttler = CaptureThrottler()
    private lateinit var classifier: TFLiteClassifier
    private var lastBlockTime = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val packageName = event.packageName?.toString() ?: "inconnu"

        // ════════════════════════════════════════════════════════
        // ANALYSE UNIQUEMENT DANS LES NAVIGATEURS. EN DEHORS DE L'APP ON N'ANALYSE RIEN.
        // ════════════════════════════════════════════════════════
        if (!BROWSER_PACKAGES.contains(packageName)) {
            return
        }

        // Évite le déclenchement multiple d'actions de retour en succession rapide
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBlockTime < 2000) {
            return
        }

        // Récupération de l'arborescence : source de l'événement ou de la fenêtre active en fallback
        val rootNode: AccessibilityNodeInfo? = event.source ?: rootInActiveWindow
        if (rootNode == null) return

        val rawText = extractAllText(rootNode)
        rootNode.recycle()

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

        // ════════════════════════════════════════════════════════
        // DÉTECTION DICTIONNAIRE & IA (TFLite)
        // ════════════════════════════════════════════════════════
        val isDictionaryToxic = InappropriateContentFilter.containsInappropriateContent(content.text)
        val toxicityScore = if (isDictionaryToxic) {
            Log.d(TAG, "Toxicité détectée via dictionnaire local.")
            1.0f
        } else {
            classifier.classify(content.text)
        }

        Log.d(TAG, "Score de toxicité final : $toxicityScore")

        if (toxicityScore > 0.8f) { // Seuil de blocage
            Log.w(TAG, "CONTENU TOXIQUE DÉTECTÉ ! Blocage en cours...")

            // Enregistre l'heure du blocage pour le cooldown
            lastBlockTime = System.currentTimeMillis()

            // Ferme uniquement la page active du navigateur en effectuant un retour (action système BACK)
            performGlobalAction(GLOBAL_ACTION_BACK)

            // Envoi de l'alerte par mail au parent
            sendAlertToParentByEmail(content)
        }
    }

    private fun sendAlertToParentByEmail(content: CapturedContent) {
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
            builder.append(nodeText)
            builder.append(" ")
        }

        val contentDescription = node.contentDescription?.toString()
        if (!contentDescription.isNullOrBlank()) {
            builder.append(contentDescription)
            builder.append(" ")
        }

        for (i in 0 until node.childCount) {
            val childNode = node.getChild(i)
            if (childNode != null) {
                builder.append(extractAllText(childNode))
                childNode.recycle()
            }
        }

        return builder.toString()
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service interrompu")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "ShieldMind Accessibility Service démarré avec succès")
        classifier = TFLiteClassifier(this)
    }
}
