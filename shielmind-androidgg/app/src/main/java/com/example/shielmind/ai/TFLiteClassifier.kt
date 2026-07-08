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

        // 1. Préparation de l'entrée (Type INT32 selon l'erreur Logcat)
        val input = Array(1) { IntArray(inputSequenceLength) }
        val tokens = tokenize(text)

        for (i in 0 until inputSequenceLength) {
            if (i < tokens.size) {
                input[0][i] = tokens[i]
            } else {
                input[0][i] = 0 // Padding
            }
        }

        // 2. Préparation de la sortie (Probabilité FLOAT32)
        val output = Array(1) { FloatArray(1) }

        try {
            // 3. Exécution de l'inférence
            interpreter?.run(input, output)
            val score = output[0][0]
            Log.d("TFLiteClassifier", "Score d'inférence pour \"${text.take(20)}...\" : $score")

            // 4. Sécurité supplémentaire (mots-clés critiques)
            if (score < 0.8f) {
                val emergencyKeywords = listOf("porno", "porn", "sex", "1xbet", "megapari")
                if (emergencyKeywords.any { text.contains(it, ignoreCase = true) }) {
                    return 0.95f
                }
            }

            return score
        } catch (e: Exception) {
            Log.e("TFLiteClassifier", "Erreur lors de l'inférence : ${e.message}")
        }

        return 0.1f
    }

    /**
     * Convertit le texte en une liste d'entiers (tokens).
     */
    private fun tokenize(text: String): List<Int> {
        // Tokenisation par caractère simplifiée
        return text.lowercase().map { it.code }
    }
}
