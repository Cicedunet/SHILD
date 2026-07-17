package com.example.shielmind.ai

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class TFLiteClassifier(context: Context) {

    private var interpreter: Interpreter? = null
    private val inputSequenceLength = 128 // Taille attendue par votre modèle

    init {
        try {
            val modelBuffer = loadModelFile(context, "shieldmindv2.tflite")
            interpreter = Interpreter(modelBuffer)
            Log.d("TFLiteClassifier", "Modèle chargé avec succès.")

            // Log model details for debugging
            val inputTensor = interpreter?.getInputTensor(0)
            Log.d("TFLiteClassifier", "Type d'entrée attendu : ${inputTensor?.dataType()}")
        } catch (e: Exception) {
            Log.e("TFLiteClassifier", "Erreur lors du chargement du modèle : ${e.message}")
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
     * Prépare le texte (tokenisation) et l'analyse via le modèle TFLite.
     */
    fun classify(text: String): Float {
        if (interpreter == null || text.isBlank()) return 0f

        // 0. Fallback Mots-clés Prioritaires (Détection immédiate pour contenus ultra-toxiques)
        val priorityKeywords = listOf("porno", "porn", "sex", "1xbet", "megapari", "insulte", "haine", "violence")
        if (priorityKeywords.any { text.contains(it, ignoreCase = true) }) {
            Log.d("TFLiteClassifier", "Toxicité détectée via mots-clés prioritaires.")
            return 0.98f
        }

        // 1. Préparation de l'entrée (Type INT32 attendu par le modèle shieldmindv2.tflite)
        val input = Array(1) { IntArray(inputSequenceLength) }
        val tokens = tokenize(text)

        for (i in 0 until inputSequenceLength) {
            input[0][i] = if (i < tokens.size) tokens[i] else 0 // Padding avec 0
        }

        // 2. Préparation de la sortie (type INT8 / ByteArray attendu par le modèle quantized avec shape [1, 8])
        val output = Array(1) { ByteArray(8) }

        try {
            // 3. Exécution de l'inférence
            interpreter?.run(input, output)

            // Les classes 1 à 7 correspondent aux catégories de toxicité/inapproprié (violence, porn, haine, drogues, etc.)
            // La classe 0 correspond au contenu sain (clean)
            // On déquantifie le byte signé de [-128, 127] vers un score float de [0.0, 1.0]
            val scores = output[0].map { (it.toInt() + 128) / 255.0f }

            var maxToxicScore = 0.0f
            for (i in 1 until scores.size) {
                if (scores[i] > maxToxicScore) {
                    maxToxicScore = scores[i]
                }
            }

            Log.d("TFLiteClassifier", "Scores d'inférence IA complets (déquantifiés) : $scores | Score toxique max : $maxToxicScore pour \"${text.take(20)}...\"")

            return maxToxicScore
        } catch (e: Exception) {
            Log.e("TFLiteClassifier", "Erreur lors de l'inférence : ${e.message}")
            // En cas d'erreur de l'IA, on se base sur une détection par mots-clés simplifiée par sécurité
            return if (priorityKeywords.any { text.contains(it, ignoreCase = true) }) 0.95f else 0.1f
        }
    }

    /**
     * Convertit le texte en une liste d'entiers (tokens).
     */
    private fun tokenize(text: String): List<Int> {
        // Tokenisation par caractère simplifiée
        return text.lowercase().map { it.code }
    }

    /**
     * Libère les ressources associées à l'interpréteur TFLite.
     */
    fun close() {
        try {
            interpreter?.close()
            interpreter = null
            Log.d("TFLiteClassifier", "Ressources du modèle libérées avec succès.")
        } catch (e: Exception) {
            Log.e("TFLiteClassifier", "Erreur lors de la fermeture du modèle : ${e.message}")
        }
    }
}
