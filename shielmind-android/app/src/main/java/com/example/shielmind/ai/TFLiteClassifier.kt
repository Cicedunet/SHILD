package com.example.shielmind.ai

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class TFLiteClassifier(context: Context) {

    private var interpreter: Interpreter? = null

    init {
        try {
            val modelBuffer = loadModelFile(context, "shieldmindv2.tflite")
            interpreter = Interpreter(modelBuffer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadModelFile(context: Context, modelName: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * Classifie le texte et renvoie un score de toxicité (0.0 à 1.0)
     * Note: Pour un vrai modèle NLP, il faudrait une étape de tokenisation.
     * Ici, on simule ou on utilise une entrée simplifiée si le modèle le permet.
     */
    fun classify(text: String): Float {
        if (interpreter == null) return 0f

        // Simulation d'une entrée pour le modèle (dépend de l'architecture du modèle .tflite)
        // Normalement on transformerait le texte en FloatArray ou IntArray de tokens
        val input = Array(1) { FloatArray(128) } // Exemple d'entrée attendue
        val output = Array(1) { FloatArray(1) } // Exemple de sortie (score de toxicité)

        try {
            // interpreter?.run(input, output)
            // Pour l'instant, on fait une détection basique de mots clés si le modèle n'est pas chargé proprement
            // ou on renvoie une valeur simulée basée sur le texte pour la démo
            val toxicKeywords = listOf("insulte", "violence", "haine", "drogue", "tuer", "mort")
            if (toxicKeywords.any { text.contains(it, ignoreCase = true) }) {
                return 0.9f
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return 0.1f
    }
}
