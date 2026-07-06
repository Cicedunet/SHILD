package com.example.shielmind.service

import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log

object FirebaseSyncManager {
    private val db = FirebaseFirestore.getInstance()

    /**
     * Lie un enfant à un parent via un identifiant unique.
     */
    fun linkChildToParent(parentId: String, childId: String, onComplete: (Boolean) -> Unit) {
        val linkingData = hashMapOf(
            "parentId" to parentId,
            "childId" to childId,
            "linkedAt" to System.currentTimeMillis()
        )
        db.collection("links").document(childId).set(linkingData)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Envoie une alerte de contenu bloqué au Firestore pour que le parent puisse la voir.
     */
    fun reportBlockedContent(childId: String, parentId: String, contentText: String, sourceApp: String) {
        val alert = hashMapOf(
            "childId" to childId,
            "parentId" to parentId,
            "text" to contentText,
            "app" to sourceApp,
            "timestamp" to System.currentTimeMillis(),
            "status" to "blocked" // Statuts: blocked, approved, dismissed
        )
        db.collection("alerts").add(alert)
    }

    /**
     * Écoute les changements de décision du parent pour un contenu spécifique.
     */
    fun listenForParentDecision(childId: String, onDecision: (String) -> Unit) {
        db.collection("alerts")
            .whereEqualTo("childId", childId)
            .whereEqualTo("status", "approved")
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                for (doc in snapshots!!) {
                    onDecision(doc.getString("text") ?: "")
                    // Une fois approuvé, on peut nettoyer ou marquer comme "notified"
                    doc.reference.update("status", "notified")
                }
            }
    }
}
