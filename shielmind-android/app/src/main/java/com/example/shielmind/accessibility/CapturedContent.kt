package com.example.shielmind.accessibility

// ════════════════════════════════════════════════════════
// À CRÉER DANS : app/src/main/java/com/example/shielmind/accessibility/CapturedContent.kt
//
// Objectif : définir UNE structure claire et stable que tout le monde
// dans l'équipe utilise pour échanger les données capturées.
// Répond à EF-NOTIF-01 du cahier des charges (horodatage, app, score).
// ════════════════════════════════════════════════════════

/**
 * "data class" = classe spéciale en Kotlin pour stocker des données.
 * Elle génère AUTOMATIQUEMENT : toString(), equals(), constructeur, etc.
 * C'est l'équivalent d'une classe avec juste des champs + getters/setters
 * en Java, mais en une seule ligne au lieu de 50.
 *
 * C'est CET objet que Membre 1 recevra pour lancer son modèle NLP,
 * et que Membre 4 utilisera pour les tests d'intégration.
 */
data class CapturedContent(
    val text: String,           // Le texte nettoyé, prêt pour tokenisation
    val sourceApp: String,      // Nom du package, ex: "com.android.chrome"
    val timestamp: Long,        // Horodatage de capture (epoch millis)
    val characterCount: Int     // Longueur du texte (utile pour les logs/debug)
) {
    /**
     * "companion object" = fonctions "statiques" rattachées à la classe.
     * On peut appeler CapturedContent.create(...) sans instance.
     */
    companion object {
        /**
         * Fonction "fabrique" : construit un CapturedContent proprement,
         * en calculant automatiquement le timestamp et characterCount.
         * Évite que chaque développeur de l'équipe le fasse différemment.
         */
        fun create(text: String, sourceApp: String): CapturedContent {
            return CapturedContent(
                text = text,
                sourceApp = sourceApp,
                timestamp = System.currentTimeMillis(),
                characterCount = text.length
            )
        }
    }
}

/**
 * Représente le résultat APRÈS analyse par le modèle NLP de Membre 1.
 * Cette classe n'est pas encore utilisée aujourd'hui (Membre 1 ne nous
 * a pas encore livré le modèle intégré), mais on la prépare maintenant
 * pour que l'intégration soit fluide quand son modèle sera prêt.
 *
 * Elle correspond exactement à EF-NOTIF-01 :
 * "journaliser chaque blocage (horo-datage, application, score)"
 */
data class AnalysisResult(
    val content: CapturedContent,   // Le contenu original analysé
    val toxicityScore: Float,       // Score du modèle, entre 0.0 et 1.0
    val decision: Decision          // Décision finale prise
)

/**
 * "enum class" = liste fermée de valeurs possibles.
 * Empêche d'écrire une décision invalide par erreur (ex: une faute
 * de frappe sur un String comme "blocked" vs "Blocked" vs "BLOCK").
 */
enum class Decision {
    SAFE,       // Contenu sûr, rien à faire
    SUSPECT,    // Zone grise, à journaliser sans bloquer
    BLOCKED     // Contenu inapproprié, blocage + alerte parent
}