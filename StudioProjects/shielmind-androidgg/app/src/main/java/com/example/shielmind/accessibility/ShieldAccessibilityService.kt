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
            "com.sec.android.app.sbrowser",
            "com.duckduckgo.mobile.android",
            "com.brave.browser",
            "com.android.browser",
            "org.mozilla.focus"
        )
    }

    private val throttler = CaptureThrottler()
    private lateinit var classifier: TFLiteClassifier
    private val throttler = CaptureThrottler()
    private lateinit var classifier: TFLiteClassifier

    // ════════════════════════════════════════════════════════
// CONFIGURATION DE L'EXPÉDITEUR (Gmail SMTP)
// ════════════════════════════════════════════════════════
    private val SENDER_EMAIL = "bouonou4@gmail.com" // Ton email d'expédition
    private val SENDER_PASSWORD = "qeuu ejov wkjj gwfw" // Ton mot de passe d'application
    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "ShieldMind Accessibility Service démarré avec succès")
        classifier = TFLiteClassifier(this)

        // Initialisation de la config expéditeur si ton EmailSender le permet
        // Ou injection directe des constantes si nécessaire
    }

    override fun onDestroy() {
        super.onDestroy()
        // Libération de la mémoire du modèle IA pour éviter de faire ramer le téléphone
        if (::classifier.isInitialized) {
            classifier.close()
        }
        Log.d(TAG, "Service ShieldMind arrêté proprement")
    }

    class ShieldAccessibilityService : AccessibilityService() {

        companion object {
            private const val TAG = "ShieldMind_Service"

            // Liste des navigateurs à surveiller
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

        // ════════════════════════════════════════════════════════
        // CONFIGURATION DE L'EXPÉDITEUR (Gmail SMTP)
        // ════════════════════════════════════════════════════════
        private val SENDER_EMAIL = "bouonou4@gmail.com"
        private val SENDER_PASSWORD = "qeuu ejov wkjj gwfw"

        override fun onServiceConnected() {
            super.onServiceConnected()
            Log.d(TAG, "ShieldMind Accessibility Service démarré avec succès")
            classifier = TFLiteClassifier(this)

            // Initialisation forcée des emails pour tes tests si les préférences sont vides
            val prefs = getSharedPreferences("shieldmind_prefs", MODE_PRIVATE)
            if (prefs.getString("parent_email", "").isNullOrBlank()) {
                prefs.edit().putString("parent_email", "fgghh8202@gmail.com").apply()
                prefs.edit().putString("child_email", "enfant_test@gmail.com").apply()
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            if (::classifier.isInitialized) {
                classifier.close()
            }
            Log.d(TAG, "Service ShieldMind arrêté proprement")
        }

        override fun onAccessibilityEvent(event: AccessibilityEvent?) {
            if (event == null) return

            val packageName = event.packageName?.toString() ?: "inconnu"

            // Analyse uniquement si l'utilisateur est dans un navigateur
            if (!BROWSER_PACKAGES.contains(packageName)) {
                return
            }

            val rootNode: AccessibilityNodeInfo? = event.source ?: rootInActiveWindow
            if (rootNode == null) return

            val rawText = extractAllText(rootNode)
            // rootNode.recycle() // Retiré pour éviter des crashs sur certaines versions d'Android

            if (rawText.isBlank()) return

            val cleanedText = TextNoiseFilter.clean(rawText)
            if (!TextNoiseFilter.isWorthAnalyzing(cleanedText)) return
            if (!throttler.shouldAnalyze(cleanedText)) return

            val content = CapturedContent.create(
                text = cleanedText,
                sourceApp = packageName
            )

            // Détection de toxicité
            val isDictionaryToxic =
                InappropriateContentFilter.containsInappropriateContent(content.text)
            val toxicityScore = if (isDictionaryToxic) 1.0f else classifier.classify(content.text)

            if (toxicityScore > 0.8f) {
                Log.w(TAG, "BLOCAGE : Contenu toxique détecté (Score: $toxicityScore)")

                // Action immédiate : Fermer la page
                performGlobalAction(GLOBAL_ACTION_BACK)

                // Alerte Parent
                sendAlertToParentByEmail(content)
            }
        }

        private fun sendAlertToParentByEmail(content: CapturedContent) {
            val prefs = getSharedPreferences("shieldmind_prefs", MODE_PRIVATE)
            val childEmail = prefs.getString("child_email", "Enfant inconnu") ?: ""
            val parentEmail = prefs.getString("parent_email", "fgghh8202@gmail.com") ?: ""

            val subject = "[ShieldMind] Alerte : Contenu inapproprié bloqué"
            val bodyText = """
            Alerte ShieldMind
            
            Un contenu inapproprié a été détecté sur l'appareil de votre enfant ($childEmail).
            
            Application : ${content.sourceApp}
            Texte analysé : ${content.text.take(500)}...
            
            Action : La page a été fermée automatiquement.
        """.trimIndent()

            // Utilisation de tes identifiants configurés dans EmailSender
            EmailSender.sendEmail(
                context = this,
                recipientEmail = parentEmail,
                subject = subject,
                bodyText = bodyText,
                onSuccess = { Log.i(TAG, "Email envoyé à $parentEmail") },
                onFailure = { e -> Log.e(TAG, "Erreur email : ${e.message}") }
            )
        }

        private fun extractAllText(node: AccessibilityNodeInfo): String {
            val builder = StringBuilder()
            val nodeText = node.text?.toString()
            if (!nodeText.isNullOrBlank()) builder.append(nodeText).append(" ")

            val contentDescription = node.contentDescription?.toString()
            if (!contentDescription.isNullOrBlank()) builder.append(contentDescription).append(" ")

            for (i in 0 until node.childCount) {
                val childNode = node.getChild(i)
                if (childNode != null) {
                    builder.append(extractAllText(childNode))
                }
            }
            return builder.toString()
        }

        override fun onInterrupt() {}
    }

    class ShieldAccessibilityService : AccessibilityService() {

        companion object {
            private const val TAG = "ShieldMind_Service"

            // Liste des navigateurs à surveiller
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

        // ════════════════════════════════════════════════════════
        // CONFIGURATION DE L'EXPÉDITEUR (Gmail SMTP)
        // ════════════════════════════════════════════════════════
        private val SENDER_EMAIL = "bouonou4@gmail.com"
        private val SENDER_PASSWORD = "qeuu ejov wkjj gwfw"

        override fun onServiceConnected() {
            super.onServiceConnected()
            Log.d(TAG, "ShieldMind Accessibility Service démarré avec succès")
            classifier = TFLiteClassifier(this)

            // Configuration forcée des emails pour tes tests
            val prefs = getSharedPreferences("shieldmind_prefs", MODE_PRIVATE)
            prefs.edit().apply {
                putString("parent_email", "fgghh8202@gmail.com")
                putString("child_email", "enfant_test@gmail.com")
                apply()
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            if (::classifier.isInitialized) {
                classifier.close()
            }
            Log.d(TAG, "Service ShieldMind arrêté proprement")
        }

        override fun onAccessibilityEvent(event: AccessibilityEvent?) {
            if (event == null) return

            val packageName = event.packageName?.toString() ?: "inconnu"

            // Analyse uniquement si l'utilisateur est dans un navigateur
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

            // Détection de toxicité (Dictionnaire puis IA)
            val isDictionaryToxic =
                InappropriateContentFilter.containsInappropriateContent(content.text)
            val toxicityScore = if (isDictionaryToxic) 1.0f else classifier.classify(content.text)

            if (toxicityScore > 0.8f) {
                Log.w(TAG, "BLOCAGE : Contenu toxique détecté (Score: $toxicityScore)")

                // Action immédiate : Retour arrière pour fermer la page
                performGlobalAction(GLOBAL_ACTION_BACK)

                // Alerte Parent
                sendAlertToParentByEmail(content)
            }
        }

        private fun sendAlertToParentByEmail(content: CapturedContent) {
            val prefs = getSharedPreferences("shieldmind_prefs", MODE_PRIVATE)
            val childEmail = prefs.getString("child_email", "Enfant") ?: "Enfant"
            val parentEmail =
                prefs.getString("parent_email", "fgghh8202@gmail.com") ?: "fgghh8202@gmail.com"

            val subject = "[ShieldMind] Alerte : Contenu inapproprié bloqué"
            val bodyText = """
            Alerte de sécurité ShieldMind
            
            Un contenu inapproprié a été détecté et bloqué sur l'appareil de votre enfant ($childEmail).
            
            Détails :
            - Application : ${content.sourceApp}
            - Texte analysé : ${content.text.take(500)}...
            
            Action : La page a été fermée automatiquement.
        """.trimIndent()

            EmailSender.sendEmail(
                context = this,
                recipientEmail = parentEmail,
                subject = subject,
                bodyText = bodyText,
                onSuccess = { Log.i(TAG, "Email d'alerte envoyé à $parentEmail") },
                onFailure = { e -> Log.e(TAG, "Échec envoi email : ${e.message}") }
            )
        }

        private fun extractAllText(node: AccessibilityNodeInfo): String {
            val builder = StringBuilder()
            val nodeText = node.text?.toString()
            if (!nodeText.isNullOrBlank()) builder.append(nodeText).append(" ")

            val contentDescription = node.contentDescription?.toString()
            if (!contentDescription.isNullOrBlank()) builder.append(contentDescription).append(" ")

            for (i in 0 until node.childCount) {
                val childNode = node.getChild(i)
                if (childNode != null) {
                    builder.append(extractAllText(childNode))
                    // On ne recycle pas ici pour éviter des erreurs pendant la récursion
                }
            }
            return builder.toString()
        }

        override fun onInterrupt() {
            Log.d(TAG, "Service interrompu")
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val packageName = event.packageName?.toString() ?: "inconnu"

        // ════════════════════════════════════════════════════════
        // ANALYSE UNIQUEMENT DANS LES NAVIGATEURS NAVIGATEUR EN DEHORS DE L'APP ON N'ANALYSE RIEN
        // ════════════════════════════════════════════════════════
        if (!BROWSER_PACKAGES.contains(packageName)) {
            return
        }

        val rootNode: AccessibilityNodeInfo? = event.source
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

            // Ferme uniquement l'onglet/page active du navigateur en effectuant un retour
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
