package com.example.shielmind.admin

// ════════════════════════════════════════════════════════
// FICHIER : app/src/main/java/com/example/shielmind/admin/DeviceAdminHelper.kt
//
// RÔLE : Classe utilitaire (singleton) qui encapsule toute
//        la logique liée au Device Administrator :
//        - Vérifier si l'app est admin
//        - Lancer l'Intent d'activation
//        - Retirer le statut admin (uniquement via PIN parent)
//
// UTILISÉ PAR : ServiceSetupScreen.kt (UI) et MainActivity.kt
// ════════════════════════════════════════════════════════

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log

object DeviceAdminHelper {

    private const val TAG = "DeviceAdminHelper"

    /**
     * Retourne le ComponentName du receiver Device Admin.
     * Android en a besoin pour identifier notre administrateur.
     */
    fun getComponentName(context: Context): ComponentName {
        return ComponentName(context, ShieldDeviceAdminReceiver::class.java)
    }

    /**
     * Vérifie si ShieldMind est actuellement administrateur de l'appareil.
     *
     * @return true si l'app a les droits admin, false sinon
     */
    fun isDeviceAdmin(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val isAdmin = dpm.isAdminActive(getComponentName(context))
        Log.d(TAG, "Statut Device Admin : $isAdmin")
        return isAdmin
    }

    /**
     * Lance l'écran système Android pour demander à l'utilisateur
     * d'activer ShieldMind comme administrateur de l'appareil.
     *
     * Un dialog Android natif s'affiche avec :
     *  - Le message d'explication (EXTRA_ADD_EXPLANATION)
     *  - Les boutons "Activer" / "Annuler"
     *
     * @param context  Contexte Android
     * @param activity L'Activity qui lance l'Intent (pour startActivityForResult)
     * @param requestCode Code de retour pour onActivityResult (utilise 1001 par convention)
     */
    fun requestDeviceAdmin(context: Context, activity: android.app.Activity, requestCode: Int = REQUEST_CODE_DEVICE_ADMIN) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

        if (dpm.isAdminActive(getComponentName(context))) {
            Log.d(TAG, "ShieldMind est déjà administrateur, rien à faire.")
            return
        }

        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, getComponentName(context))
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "ShieldMind a besoin de droits administrateur pour empêcher " +
                        "sa désinstallation non autorisée par l'enfant. " +
                        "Cette protection est essentielle pour garantir la sécurité numérique."
            )
        }

        Log.d(TAG, "Lancement de l'écran d'activation Device Admin...")
        activity.startActivityForResult(intent, requestCode)
    }

    /**
     * VERSION JETPACK COMPOSE : Lance l'activation Device Admin
     * sans passer par startActivityForResult (utilise un launcher Compose).
     *
     * À utiliser dans les Composables avec rememberLauncherForActivityResult.
     *
     * Exemple dans un Composable :
     * ```kotlin
     * val launcher = rememberLauncherForActivityResult(
     *     ActivityResultContracts.StartActivityForResult()
     * ) { result ->
     *     if (result.resultCode == Activity.RESULT_OK) {
     *         // Admin activé avec succès
     *     }
     * }
     * DeviceAdminHelper.buildDeviceAdminIntent(context)?.let { launcher.launch(it) }
     * ```
     */
    fun buildDeviceAdminIntent(context: Context): Intent {
        return Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, getComponentName(context))
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "ShieldMind a besoin de droits administrateur pour empêcher " +
                        "sa désinstallation non autorisée par l'enfant."
            )
        }
    }

    /**
     * Retire le statut administrateur de l'application.
     *
     * ⚠️  ATTENTION : Cette méthode NE DOIT être appelée QUE depuis
     *     le tableau de bord parent, après vérification du PIN.
     *     Ne jamais l'exposer à l'interface enfant.
     *
     * Après cet appel, l'enfant pourra désinstaller l'app.
     */
    fun removeDeviceAdmin(context: Context) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        dpm.removeActiveAdmin(getComponentName(context))
        Log.w(TAG, "⚠️ Droits administrateur retirés par le parent.")
    }

    // Code de requête conventionnel pour startActivityForResult
    const val REQUEST_CODE_DEVICE_ADMIN = 1001
}