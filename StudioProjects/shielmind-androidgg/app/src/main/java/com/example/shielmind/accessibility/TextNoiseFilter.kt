package com.example.shielmind.accessibility

// ════════════════════════════════════════════════════════
// À CRÉER DANS : app/src/main/java/com/example/shielmind/accessibility/TextNoiseFilter.kt
//
// Objectif : retirer les éléments d'interface qui n'apportent
// AUCUNE information utile pour le modèle NLP de Membre 1.
// Construit directement à partir de tes vrais logs de test.
// ════════════════════════════════════════════════════════

/**
 * "object" = singleton, pas besoin d'instancier.
 * On appelle directement TextNoiseFilter.clean(...)
 */
object TextNoiseFilter {

    /**
     * Liste des expressions qu'on a VUES dans tes logs réels et qui
     * ne sont que du bruit d'interface (boutons, labels système).
     *
     * NOTE KOTLIN : "listOf(...)" crée une liste immuable (non modifiable).
     * C'est l'équivalent d'un "List.of(...)" en Java moderne, ou d'un
     * tableau "final" qu'on ne peut pas réassigner.
     */
    private val UI_NOISE_PATTERNS = listOf(
        // Vu dans tes logs Facebook
        "Like button. Double tap and hold to react to the comment.",
        "Share button. Double tap to share the post.",
        "Double tap to create a new post, story, or reel",
        "More options for",
        "Hide post",
        "profile picture",
        "Open .*'s story, unseen story".toRegex(),  // motif avec nom variable
        "Home, tab \\d of \\d".toRegex(),
        "Reels, tab \\d of \\d".toRegex(),
        "Friends, tab \\d of \\d".toRegex(),
        "Marketplace, tab \\d of \\d".toRegex(),
        "Notifications, tab \\d of \\d".toRegex(),
        "Profile, tab \\d of \\d".toRegex(),

        // Vu dans tes logs TikTok
        "Clear search field",
        "Mute sound",
        "Sound is on Mute",
        "Play current reel",

        // Vu dans tes logs Chrome
        "Open the home page",
        "Open the context popup",
        "Connection is secure",
        "Customize and control Google Chrome",
        "See \\d+ tabs".toRegex(),
        "Signed in as .*".toRegex(),

        // Vu dans tes logs système (barre de statut, batterie...)
        "Orange CM, .* bars?\\.".toRegex(),
        "Orange CM, no signal\\.",
        "Battery \\d+ percent\\.".toRegex(),
        "Hotspot Orange",
        ".* notification: *$".toRegex(),  // ex: "WhatsApp notification: "

        // Lanceur d'applications (écran d'accueil)
        "More optionsButton",
        "Close all",
        "Advanced options,.*Button".toRegex(),
        "Page \\d of \\d\\.".toRegex(),
        ", Folder$".toRegex(),
        ", \\d+ notifications?".toRegex()
    )

    /**
     * Nettoie un texte brut en retirant tout le bruit identifié.
     *
     * @param rawText le texte tel que capturé par l'Accessibility Service
     * @return le texte nettoyé, prêt pour la tokenisation
     */
    fun clean(rawText: String): String {

        var result = rawText

        // On parcourt chaque motif de bruit et on le retire
        for (pattern in UI_NOISE_PATTERNS) {
            result = when (pattern) {
                // Si c'est une expression régulière (Regex)
                is kotlin.text.Regex -> pattern.replace(result, "")
                // Si c'est un texte simple (String)
                is String -> result.replace(pattern, "", ignoreCase = true)
                else -> result
            }
        }

        // Nettoyage final : on retire les espaces multiples laissés
        // par les suppressions, et les espaces en début/fin
        result = result.replace(Regex("\\s+"), " ").trim()

        return result
    }

    /**
     * Vérifie si un texte, une fois nettoyé, vaut encore la peine
     * d'être analysé par le modèle NLP.
     *
     * Évite d'envoyer des chaînes vides ou ridiculement courtes
     * (ex: juste un chiffre de compteur de likes "234") au modèle.
     */
    fun isWorthAnalyzing(cleanedText: String): Boolean {
        // On ignore les textes trop courts (moins de 10 caractères)
        if (cleanedText.length < 10) return false

        // On ignore les textes qui ne contiennent que des chiffres/symboles
        // (ex: "160 160", "6:32 AM", "+3 +3")
        val hasLetters = cleanedText.any { it.isLetter() }
        if (!hasLetters) return false

        return true
    }
}