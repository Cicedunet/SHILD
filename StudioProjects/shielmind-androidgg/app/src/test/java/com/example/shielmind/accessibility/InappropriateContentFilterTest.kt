package com.example.shielmind.accessibility

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InappropriateContentFilterTest {

    @Test
    fun testSafeContent() {
        assertFalse(InappropriateContentFilter.containsInappropriateContent("Bonjour, comment allez-vous ?"))
        assertFalse(InappropriateContentFilter.containsInappropriateContent("La programmation en Kotlin est fantastique."))
        assertFalse(InappropriateContentFilter.containsInappropriateContent("Le ciel est bleu aujourd'hui."))
    }

    @Test
    fun testSingleInappropriateKeywords() {
        assertTrue(InappropriateContentFilter.containsInappropriateContent("C'est un site porno."))
        assertTrue(InappropriateContentFilter.containsInappropriateContent("Recherche de sextoy sur internet."))
        assertTrue(InappropriateContentFilter.containsInappropriateContent("Consommation de cannabis."))
        assertTrue(InappropriateContentFilter.containsInappropriateContent("Recherche de cocaine."))
        assertTrue(InappropriateContentFilter.containsInappropriateContent("pornhub est un site très connu."))
    }

    @Test
    fun testWordCombinations() {
        // "acheter" + "drogue"
        assertTrue(InappropriateContentFilter.containsInappropriateContent("Où peut-on acheter de la drogue ?"))

        // "regarder" + "porno"
        assertTrue(InappropriateContentFilter.containsInappropriateContent("Il passe son temps à regarder des vidéos porno."))

        // "video" + "sexe"
        assertTrue(InappropriateContentFilter.containsInappropriateContent("Voici une nouvelle vidéo de sexe amateur."))

        // "deal" + "drogue"
        assertTrue(InappropriateContentFilter.containsInappropriateContent("Un suspect arrêté pour deal de drogue."))
    }

    @Test
    fun testIncompleteWordCombinations() {
        // "acheter" without "drogue"
        assertFalse(InappropriateContentFilter.containsInappropriateContent("Je vais acheter une nouvelle voiture."))

        // "drogue" alone (is inappropriate on its own in the dictionary anyway!)
        assertTrue(InappropriateContentFilter.containsInappropriateContent("La drogue est un fléau."))

        // "regarder" without "porno"
        assertFalse(InappropriateContentFilter.containsInappropriateContent("J'adore regarder les étoiles."))
    }

    @Test
    fun testCaseInsensitivityAndNormalisation() {
        assertTrue(InappropriateContentFilter.containsInappropriateContent("PORNO"))
        assertTrue(InappropriateContentFilter.containsInappropriateContent("SeXe"))
        assertTrue(InappropriateContentFilter.containsInappropriateContent("AchEtEr de la DroGuE"))
    }
}
