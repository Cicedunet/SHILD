package com.example.shielmind.accessibility

object InappropriateContentFilter {

    // Single keywords related to pornography, sex, and drugs (matched as whole words)
    private val INAPPROPRIATE_KEYWORDS = setOf(
        "porno", "pornographie", "porn", "xxx", "hentai", "milf", "redtube",
        "pornhub", "xvideos", "jacquie", "youporn", "erotique", "érotique",
        "sexe", "sex", "baiser", "nique", "pénétration", "masturbation", "orgasme",
        "coit", "coït", "escort", "fellation", "cunnilingus", "gode", "sextoy",
        "drogue", "drogues", "cannabis", "weed", "cocaïne", "cocaine", "héroïne", "heroine",
        "ecstasy", "mdma", "shite", "chite", "marijuana", "stupéfiant", "lsd", "meth"
    )

    // Keywords that can be matched as substrings because they represent betting websites/domains
    private val SUBSTRING_KEYWORDS = listOf(
        "1xbet", "megapari"
    )

    // Word combinations where all words in the sublist must be present in the text to trigger detection
    private val WORD_COMBINATIONS = listOf(
        listOf("acheter", "drogue"),
        listOf("acheter", "weed"),
        listOf("acheter", "cannabis"),
        listOf("acheter", "cocaïne"),
        listOf("acheter", "cocaine"),
        listOf("regarder", "porno"),
        listOf("regarder", "sexe"),
        listOf("regarder", "sex"),
        listOf("video", "sexe"),
        listOf("vidéo", "sexe"),
        listOf("film", "porno"),
        listOf("site", "porno"),
        listOf("site", "sexe"),
        listOf("consommer", "drogue"),
        listOf("consommation", "drogue"),
        listOf("achat", "drogue"),
        listOf("achat", "weed"),
        listOf("achat", "cannabis"),
        listOf("deal", "drogue")
    )

    /**
     * Checks if the given text contains any inappropriate content based on the dictionary
     * using whole-word matching to avoid false positives (e.g. "technique" containing "nique").
     */
    fun containsInappropriateContent(text: String): Boolean {
        if (text.isBlank()) return false
        val normalized = text.lowercase()

        // 1. Check for specific betting/domain substring keywords
        for (sub in SUBSTRING_KEYWORDS) {
            if (normalized.contains(sub)) {
                return true
            }
        }

        // 2. Extract whole words using non-alphanumeric split (retaining French accents)
        val words = normalized.split(Regex("[^a-zA-Z0-9àâäéèêëîïôöùûüç]"))
            .filter { it.isNotBlank() }

        // 3. Check for single inappropriate keywords as whole words
        for (word in words) {
            if (INAPPROPRIATE_KEYWORDS.contains(word)) {
                return true
            }
        }

        // 4. Check for word combinations (all words of a combination must be present as whole words)
        val wordSet = words.toSet()
        for (combination in WORD_COMBINATIONS) {
            if (combination.all { wordSet.contains(it) }) {
                return true
            }
        }

        return false
    }
}
