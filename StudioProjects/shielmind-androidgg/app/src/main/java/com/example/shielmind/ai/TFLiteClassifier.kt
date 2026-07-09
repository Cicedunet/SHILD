package com.example.shielmind.ai

import android.content.Context
import android.util.Log
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class TFLiteClassifier(context: Context) {

    private var interpreter: Interpreter? = null
    private val inputSequenceLength = 128

    init {
        try {
            val modelBuffer = loadModelFile(context, "shieldmindv2.tflite")
            val options = Interpreter.Options()
            options.setNumThreads(4)
            interpreter = Interpreter(modelBuffer, options)
            
            val inputTensor = interpreter?.getInputTensor(0)
            val outputTensor = interpreter?.getOutputTensor(0)
            
            Log.i("TFLiteClassifier", "Modèle chargé avec succès.")
            Log.i("TFLiteClassifier", "Format Entrée: ${inputTensor?.dataType()} Shape: ${inputTensor?.shape()?.contentToString()}")
            Log.i("TFLiteClassifier", "Format Sortie: ${outputTensor?.dataType()} Shape: ${outputTensor?.shape()?.contentToString()}")
        } catch (e: Exception) {
            Log.e("TFLiteClassifier", "Erreur lors du chargement : ${e.message}")
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

    fun classify(text: String): Float {
        val interp = interpreter ?: return 0.1f
        if (text.isBlank()) return 0.05f

        try {
            val inputTensor = interp.getInputTensor(0)
            val outputTensor = interp.getOutputTensor(0)

            // 1. Préparation du Buffer d'entrée (INT32 ou INT8 selon le modèle)
            val inputBuffer = ByteBuffer.allocateDirect(inputTensor.numBytes())
            inputBuffer.order(ByteOrder.nativeOrder())

            val tokens = tokenize(text)
            for (i in 0 until inputSequenceLength) {
                val token = if (i < tokens.size) tokens[i] else 0
                if (inputTensor.dataType() == DataType.INT32) {
                    inputBuffer.putInt(token)
                } else {
                    inputBuffer.put(token.toByte())
                }
            }
            inputBuffer.rewind()

            // 2. Préparation du Buffer de sortie (INT8 pour votre modèle)
            val outputBuffer = ByteBuffer.allocateDirect(outputTensor.numBytes())
            outputBuffer.order(ByteOrder.nativeOrder())

            // 3. Inférence via les buffers directs (évite les erreurs de conversion Java array)
            interp.run(inputBuffer, outputBuffer)

            // 4. Lecture et déquantification
            outputBuffer.rewind()
            var score: Float
            
            if (outputTensor.dataType() == DataType.FLOAT32) {
                score = outputBuffer.float
            } else {
                // Modèle quantifié : score = (valeur_brute - zero_point) * scale
                val rawByte = outputBuffer.get().toInt()
                val params = outputTensor.quantizationParams()
                
                if (params.scale != 0f) {
                    score = (rawByte - params.zeroPoint) * params.scale
                } else {
                    // Fallback si les métadonnées de quantification sont absentes
                    score = (rawByte.toFloat() + 128f) / 255.0f
                }
            }

            Log.d("TFLiteClassifier", "Texte: \"${text.take(20)}\" -> Score IA: $score")

            // 5. Filtre de sécurité par mots-clés
            val blacklist = listOf("porno", "porn", "sex", "1xbet", "megapari", "casino")
            if (blacklist.any { text.contains(it, ignoreCase = true) }) {
                return if (score > 0.9f) score else 0.99f
            }

            return score
        } catch (e: Exception) {
            Log.e("TFLiteClassifier", "Erreur inférence : ${e.message}")
            return if (text.contains("porn", ignoreCase = true)) 0.99f else 0.1f
        }
    }

    private fun tokenize(text: String): List<Int> {
        // Tokenisation simple par code de caractère (Unicode)
        // Note: Si le modèle a été entraîné avec un vocabulaire spécifique, 
        // cette fonction doit être remplacée par une recherche dans vocab.txt.
        return text.lowercase().map { it.code }
    }
}
