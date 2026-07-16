package com.example.shielmind.accessibility

object InappropriateContentFilter {

    // Single keywords related to pornography, sex, and drugs
    private val INAPPROPRIATE_KEYWORDS = listOf(
        "porno", "pornographie", "porn", "xxx", "hentai", "milf", "redtube",
        "pornhub", "xvideos", "jacquie et michel", "youporn", "erotique", "érotique",
        "sexe", "sex", "baiser", "nique", "pénétration", "masturbation", "orgasme",
        "coit", "coït", "escort", "fellation", "cunnilingus", "gode", "sextoy",
        "drogue", "drogues", "cannabis", "weed", "cocaïne", "cocaine", "héroïne", "heroine",
        "ecstasy", "mdma", "shite", "chite", "marijuana", "stupéfiant", "lsd", "meth", "1xbet", "megapari"
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
     * or any of the predefined word combinations.
     */
    fun containsInappropriateContent(text: String): Boolean {
        if (text.isBlank()) return false
        val normalized = text.lowercase()

        // 1. Check for single inappropriate keywords
        for (keyword in INAPPROPRIATE_KEYWORDS) {
            if (normalized.contains(keyword)) {
                return true
            }
        }

        // 2. Check for word combinations
        for (combination in WORD_COMBINATIONS) {
            if (combination.all { word -> normalized.contains(word) }) {
                return true
            }
        }

        return false
    }
}
