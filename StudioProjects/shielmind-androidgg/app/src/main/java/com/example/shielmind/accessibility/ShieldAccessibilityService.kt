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
    private var cachedParentId: String? = null

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
        // Envoi d'un broadcast ou démarrage d'activité pour bloquer
        // Pour une fiabilité maximale en arrière-plan, on utilise FLAG_ACTIVITY_CLEAR_TOP
        val intent = android.content.Intent(this, com.example.shielmind.MainActivity::class.java).apply {
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("BLOCK_REASON", reason)
        }
        startActivity(intent)
        Log.i(TAG, "Écran de blocage activé pour l'application en cours.")
    }

    private fun sendAlertToParent(content: CapturedContent) {
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val currentChildId = auth.currentUser?.uid ?: return

        if (cachedParentId != null) {
            com.example.shielmind.service.FirebaseSyncManager.reportBlockedContent(
                childId = currentChildId,
                parentId = cachedParentId!!,
                contentText = content.text,
                sourceApp = content.sourceApp
            )
            return
        }

        // On cherche le parent lié dans Firestore
        db.collection("links").document(currentChildId).get()
            .addOnSuccessListener { doc ->
                val parentId = doc.getString("parentId")
                if (parentId != null) {
                    cachedParentId = parentId
                    com.example.shielmind.service.FirebaseSyncManager.reportBlockedContent(
                        childId = currentChildId,
                        parentId = parentId,
                        contentText = content.text,
                        sourceApp = content.sourceApp
                    )
                    Log.i(TAG, "Alerte synchronisée sur Firebase pour le parent : $parentId")
                } else {
                    Log.w(TAG, "Aucun parent lié trouvé pour cet enfant.")
                }
            }
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
