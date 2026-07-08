package com.example.shielmind.admin

// ════════════════════════════════════════════════════════
// FICHIER : app/src/main/java/com/example/shielmind/admin/ShieldDeviceAdminReceiver.kt
//
// RÔLE : Receiver qui reçoit les événements liés au statut
//        d'administrateur de l'appareil.
//        C'est lui qui est enregistré comme "Device Admin" dans
//        le système Android.
//
// IMPORTANT : Ce receiver doit être déclaré dans AndroidManifest.xml
//             avec la permission BIND_DEVICE_ADMIN.
// ════════════════════════════════════════════════════════

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

/**
 * Receiver Device Administrator pour ShieldMind.
 *
 * DeviceAdminReceiver est la classe Android qui gère les événements
 * liés au statut administrateur de l'application :
 *  - onEnabled()  → appelé quand le parent active le mode admin
 *  - onDisabled() → appelé quand quelqu'un tente de désactiver le mode admin
 *  - onDisableRequested() → appelé AVANT la désactivation (permet d'afficher un message)
 */
class ShieldDeviceAdminReceiver : DeviceAdminReceiver() {

    companion object {
        private const val TAG = "ShieldDeviceAdmin"
    }

    /**
     * Appelé quand l'application est activée comme administrateur.
     * C'est ici que tu peux initialiser des protections supplémentaires.
     */
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.d(TAG, "✅ ShieldMind activé comme Administrateur de l'appareil")
        Toast.makeText(
            context,
            "✅ ShieldMind : Protection anti-désinstallation activée",
            Toast.LENGTH_LONG
        ).show()
    }

    /**
     * Appelé quand le statut administrateur est retiré.
     *
     * ATTENTION : Si l'enfant retire manuellement le droit admin
     * depuis Paramètres > Sécurité > Administrateurs, cette méthode
     * sera appelée. C'est le moment d'alerter le parent via Firebase.
     */
    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.w(TAG, "⚠️ ShieldMind n'est plus Administrateur de l'appareil !")

        // TODO (Membre 3) : Envoyer une notification Firebase au parent
        // pour l'alerter que la protection a été désactivée.
        //
        // Exemple d'appel Firebase à ajouter :
        // FirebaseNotificationHelper.sendParentAlert(
        //     context = context,
        //     title = "⚠️ Alerte ShieldMind",
        //     message = "La protection anti-désinstallation a été retirée sur l'appareil de votre enfant."
        // )

        Toast.makeText(
            context,
            "⚠️ Protection ShieldMind désactivée ! Le parent sera alerté.",
            Toast.LENGTH_LONG
        ).show()
    }

    /**
     * Appelé JUSTE AVANT que le statut admin soit retiré.
     * Retourne un message d'avertissement affiché à l'utilisateur
     * dans la boîte de dialogue de confirmation Android.
     *
     * C'est le dernier rempart avant la désactivation.
     */
    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return "⚠️ Attention ! Désactiver ShieldMind supprimera la protection " +
                "de votre enfant contre les contenus inappropriés. " +
                "Le parent sera immédiatement notifié."
    }
}