package com.example.shielmind.accessibility

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.shielmind.ai.TFLiteClassifier

class ShieldAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "ShieldMind_Service"
    }

    private val throttler = CaptureThrottler()
    private lateinit var classifier: TFLiteClassifier

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val packageName = event.packageName?.toString() ?: "inconnu"
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

        // ANALYSE IA (TFLite)
        val toxicityScore = classifier.classify(content.text)
        Log.d(TAG, "Score de toxicité détecté : $toxicityScore")

        if (toxicityScore > 0.8f) { // Seuil de blocage
            Log.w(TAG, "CONTENU TOXIQUE DÉTECTÉ ! Blocage en cours...")
            showBlockingUI(content.text)
            sendAlertToParent(content)
        }
    }

    private fun showBlockingUI(reason: String) {
        val intent = android.content.Intent(this, com.example.shielmind.MainActivity::class.java).apply {
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("BLOCK_REASON", reason)
        }
        startActivity(intent)
    }

    private fun sendAlertToParent(content: CapturedContent) {
        val currentChildId = "child_user_123"
        val linkedParentId = "parent_user_456"

        com.example.shielmind.service.FirebaseSyncManager.reportBlockedContent(
            childId = currentChildId,
            parentId = linkedParentId,
            contentText = content.text,
            sourceApp = content.sourceApp
        )
        Log.i(TAG, "Alerte synchronisée sur Firebase pour le parent.")
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
