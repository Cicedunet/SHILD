package com.example.shielmind.accessibility

// ════════════════════════════════════════════════════════
// REMPLACE le fichier existant :
// app/src/main/java/com/example/shielmind/accessibility/ShieldAccessibilityService.kt
//
// Changements par rapport à la version du Jour 1 :
// 1. Filtrage du bruit UI (TextNoiseFilter)
// 2. Déduplication / throttling (CaptureThrottler)
// 3. Construction d'un CapturedContent propre en sortie
// ════════════════════════════════════════════════════════

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class ShieldAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "ShieldMind_Service"
    }

    // Une seule instance de throttler pour toute la durée de vie du service.
    // Si on en créait une nouvelle à chaque événement, la déduplication
    // ne fonctionnerait jamais (elle "oublierait" le texte précédent).
    private val throttler = CaptureThrottler()

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val packageName = event.packageName?.toString() ?: "inconnu"
        val rootNode: AccessibilityNodeInfo? = event.source

        if (rootNode == null) {
            // On ne log même plus ce cas en détail (Jour 1 l'a déjà validé,
            // c'est un comportement normal et fréquent d'Android)
            return
        }

        // ─────────────────────────────────────────
        // ÉTAPE 1 : Extraction brute (logique du Jour 1, inchangée)
        // ─────────────────────────────────────────
        val rawText = extractAllText(rootNode)
        rootNode.recycle()

        if (rawText.isBlank()) return

        // ─────────────────────────────────────────
        // ÉTAPE 2 : Nettoyage du bruit UI (NOUVEAU - Jour 2)
        // ─────────────────────────────────────────
        val cleanedText = TextNoiseFilter.clean(rawText)

        if (!TextNoiseFilter.isWorthAnalyzing(cleanedText)) {
            // Texte vide ou sans intérêt après nettoyage (ex: juste "160 160")
            return
        }

        // ─────────────────────────────────────────
        // ÉTAPE 3 : Déduplication / throttling (NOUVEAU - Jour 2)
        // ─────────────────────────────────────────
        if (!throttler.shouldAnalyze(cleanedText)) {
            // Soit texte déjà vu, soit trop tôt depuis la dernière analyse
            return
        }

        // ─────────────────────────────────────────
        // ÉTAPE 4 : Construction de l'objet propre (NOUVEAU - Jour 2)
        // ─────────────────────────────────────────
        val content = CapturedContent.create(
            text = cleanedText,
            sourceApp = packageName
        )

        Log.d(TAG, "Contenu prêt pour analyse : app=${content.sourceApp}, " +
                "${content.characterCount} caractères, " +
                "texte=\"${content.text.take(100)}...\"")

        // ──────────────────────────────────────────────
        // POINT D'INTÉGRATION FUTUR :
        // C'est ICI qu'on appellera le modèle TFLite de Membre 1 :
        //
        //   val score = ModeleShieldMind.predict(content.text)
        //   val decision = MoteurDecision.evaluer(score, ageEnfant)
        //   val result = AnalysisResult(content, score, decision)
        //
        // En attendant que son modèle soit intégré, on se contente
        // de logger le contenu propre et structuré.
        // ──────────────────────────────────────────────
    }

    /**
     * Inchangé depuis le Jour 1 — fonctionne déjà très bien d'après tes tests.
     */
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
    }
}