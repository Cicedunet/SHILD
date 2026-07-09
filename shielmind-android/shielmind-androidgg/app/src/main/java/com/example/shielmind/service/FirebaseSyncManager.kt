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
     * Vérifie d'abord que l'enfant existe.
     */
    fun linkChildToParent(parentId: String, childId: String, onComplete: (Boolean) -> Unit) {
        val database = db ?: run {
            onComplete(false)
            return
        }

        // 1. Vérifier si l'enfant existe dans la collection users
        database.collection("users").document(childId).get()
            .addOnSuccessListener { userDoc ->
                if (userDoc.exists() && userDoc.getString("role") == "enfant") {
                    val childEmail = userDoc.getString("email") ?: "Inconnu"

                    // 2. Créer le lien
                    val linkingData = hashMapOf(
                        "parentId" to parentId,
                        "childId" to childId,
                        "childEmail" to childEmail,
                        "linkedAt" to System.currentTimeMillis()
                    )

                    database.collection("links").document(childId).set(linkingData)
                        .addOnSuccessListener {
                            Log.d("FirebaseSyncManager", "Lien créé entre $parentId et $childId")
                            onComplete(true)
                        }
                        .addOnFailureListener { onComplete(false) }
                } else {
                    Log.e("FirebaseSyncManager", "L'identifiant enfant est invalide ou n'est pas un compte enfant.")
                    onComplete(false)
                }
            }
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
            "status" to "blocked", // Statuts: blocked, approved, dismissed
            "childEmail" to (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email ?: "Inconnu")
        )
        database.collection("alerts").add(alert)
            .addOnSuccessListener { Log.d("FirebaseSyncManager", "Alerte envoyée avec succès") }
            .addOnFailureListener { e -> Log.e("FirebaseSyncManager", "Erreur envoi alerte: ${e.message}") }
    }

    /**
     * Enregistre le token FCM de l'appareil.
     */
    fun updateFcmToken(userId: String, token: String) {
        db?.collection("users")?.document(userId)?.update("fcmToken", token)
    }

    /**
     * Écoute les changements de décision du parent (Autorisation ou Confirmation de blocage).
     */
    fun listenForParentDecision(childId: String, onDecision: (String, Boolean) -> Unit) {
        val database = db ?: return
        database.collection("alerts")
            .whereEqualTo("childId", childId)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("FirebaseSyncManager", "Erreur d'écoute : ${e.message}")
                    return@addSnapshotListener
                }
                for (doc in snapshots!!) {
                    val status = doc.getString("status")
                    val text = doc.getString("text") ?: ""

                    if (status == "approved") {
                        // Le parent a autorisé : on débloque l'enfant
                        onDecision(text, true)
                        doc.reference.update("status", "notified_approved")
                    } else if (status == "confirmed") {
                        // Le parent a confirmé le blocage : on notifie juste l'enfant
                        onDecision(text, false)
                        doc.reference.update("status", "notified_confirmed")
                    }
                }
            }
    }
}
