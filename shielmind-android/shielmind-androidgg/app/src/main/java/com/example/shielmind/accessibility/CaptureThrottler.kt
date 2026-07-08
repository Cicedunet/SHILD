package com.example.shielmind.accessibility

// ════════════════════════════════════════════════════════
// À CRÉER DANS : app/src/main/java/com/example/shielmind/accessibility/CaptureThrottler.kt
//
// Objectif : résoudre EXACTEMENT le problème vu dans tes logs :
// le même texte "Fier de la Côte d'Ivoire..." capturé 15 fois
// en 2 secondes pendant un scroll Facebook.
// ════════════════════════════════════════════════════════

import android.os.SystemClock

/**
 * Empêche d'analyser plusieurs fois le même texte en peu de temps.
 *
 * STRATÉGIE (2 mécanismes combinés) :
 * 1. Déduplication : si c'est EXACTEMENT le même texte qu'avant → on ignore
 * 2. Throttling (limitation) : même si le texte change, on attend un
 *    minimum de temps entre 2 analyses, pour ne pas surcharger le CPU
 *    pendant un scroll rapide
 */
class CaptureThrottler {

    companion object {
        // Délai minimum entre 2 analyses, en millisecondes.
        // 500ms = on analyse au maximum 2 fois par seconde.
        // Ajustable selon les résultats de Membre 1 (vitesse du modèle).
        private const val MIN_INTERVAL_MS = 500L
    }

    // "var" car ces valeurs changent à chaque appel (contrairement à "val")
    private var lastAnalyzedText: String = ""
    private var lastAnalyzedTimestamp: Long = 0L

    /**
     * Décide si CE texte mérite d'être envoyé au modèle NLP maintenant.
     *
     * @param text le texte nettoyé (après TextNoiseFilter.clean)
     * @return true si on doit analyser, false si on doit ignorer
     */
    fun shouldAnalyze(text: String): Boolean {

        val now = SystemClock.elapsedRealtime()

        // Cas 1 : texte identique au précédent → on ignore directement,
        // peu importe le temps écoulé (ça reste exactement la même info)
        if (text == lastAnalyzedText) {
            return false
        }

        // Cas 2 : texte différent, mais on a analysé il y a moins de
        // MIN_INTERVAL_MS → on ignore aussi, pour limiter la fréquence
        // (utile pendant un scroll très rapide où le texte change
        // légèrement à chaque frame)
        val timeSinceLastAnalysis = now - lastAnalyzedTimestamp
        if (timeSinceLastAnalysis < MIN_INTERVAL_MS) {
            return false
        }

        // Sinon : on accepte, et on met à jour notre mémoire
        lastAnalyzedText = text
        lastAnalyzedTimestamp = now
        return true
    }
}