package com.example.shielmind.accessibility

// ════════════════════════════════════════════════════════
// À CRÉER DANS : app/src/main/java/com/example/shielmind/accessibility/AccessibilityHelper.kt
// ════════════════════════════════════════════════════════

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils

/**
 * Fonctions utilitaires pour vérifier si notre service est activé,
 * et rediriger l'utilisateur vers les paramètres Android si besoin.
 *
 * NOTE KOTLIN : "object" (au lieu de "class") = Singleton automatique.
 * Pas besoin d'instancier, on appelle directement AccessibilityHelper.maFonction()
 * C'est l'équivalent d'une classe avec uniquement des méthodes static en Java.
 */
object AccessibilityHelper {

    /**
     * Vérifie si l'utilisateur a bien activé notre service
     * dans les paramètres Android.
     *
     * Retourne "true" ou "false" (Boolean en Kotlin = boolean en Java)
     */
    fun isAccessibilityServiceEnabled(context: Context): Boolean {

        val expectedServiceName = "${context.packageName}/${ShieldAccessibilityService::class.java.canonicalName}"

        val enabledServicesSetting = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        // On découpe la liste (Android stocke plusieurs services séparés par ":")
        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServicesSetting)

        while (colonSplitter.hasNext()) {
            val serviceName = colonSplitter.next()
            if (serviceName.equals(expectedServiceName, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    /**
     * Ouvre directement l'écran "Accessibilité" des paramètres Android.
     * L'utilisateur n'a plus qu'à chercher "ShieldMind" et l'activer.
     *
     * On ne PEUT PAS activer le service automatiquement par code,
     * c'est une protection volontaire d'Android (sécurité) — l'utilisateur
     * doit toujours donner cette permission manuellement.
     */
    fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
}