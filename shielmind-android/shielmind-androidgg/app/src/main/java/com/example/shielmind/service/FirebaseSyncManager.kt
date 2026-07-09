package com.example.shielmind.service

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.FirebaseApp
import android.util.Log

object FirebaseSyncManager {
    // Initialisation paresseuse pour éviter le crash au démarrage si Firebase n'est pas prêt
    private val db: FirebaseFirestore? by lazy {
        try {
            // Vérifie si Firebase est initialisé avant de récupérer l'instance Firestore
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e("FirebaseSyncManager", "Erreur d'initialisation Firestore : ${e.message}")
            null
        }
    }

    /**
     * Lie un enfant à un parent via un identifiant unique.
     */
    fun linkChildToParent(parentId: String, childId: String, onComplete: (Boolean) -> Unit) {
        val database = db ?: run {
            onComplete(false)
            return
        }
        val linkingData = hashMapOf(
            "parentId" to parentId,
            "childId" to childId,
            "linkedAt" to System.currentTimeMillis()
        )
        database.collection("links").document(childId).set(linkingData)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Envoie une alerte de contenu bloqué au Firestore pour que le parent puisse la voir.
     */
    fun reportBlockedContent(childId: String, parentId: String, contentText: String, sourceApp: String) {
        val database = db ?: return
        val alert = hashMapOf(
            "childId" to childId,
            "parentId" to parentId,
            "text" to contentText,
            "app" to sourceApp,
            "timestamp" to System.currentTimeMillis(),
            "status" to "blocked" // Statuts: blocked, approved, dismissed
        )
        database.collection("alerts").add(alert)
    }

    /**
     * Enregistre le token FCM de l'appareil.
     */
    fun updateFcmToken(userId: String, token: String) {
        db?.collection("users")?.document(userId)?.update("fcmToken", token)
    }

    /**
     * Écoute les changements de décision du parent pour un contenu spécifique.
     */
    fun listenForParentDecision(childId: String, onDecision: (String) -> Unit) {
        val database = db ?: return
        database.collection("alerts")
            .whereEqualTo("childId", childId)
            .whereEqualTo("status", "approved")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("FirebaseSyncManager", "Erreur d'écoute : ${e.message}")
                    return@addSnapshotListener
                }
                for (doc in snapshots!!) {
                    onDecision(doc.getString("text") ?: "")
                    // Une fois approuvé, on peut nettoyer ou marquer comme "notified"
                    doc.reference.update("status", "notified")
                }
            }
    }
}
