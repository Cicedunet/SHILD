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
    private val TAG = "TFLiteClassifier"

    init {
        try {
            val modelBuffer = loadModelFile(context, "shieldmindv2.tflite")
            interpreter = Interpreter(modelBuffer)
            Log.d(TAG, "Modèle TFLite chargé avec succès.")

            // Log input/output info for debugging
            val inputTensor = interpreter?.getInputTensor(0)
            Log.d(TAG, "Input Type: ${inputTensor?.dataType()}, Shape: ${inputTensor?.shape()?.contentToString()}")
            val outputTensor = interpreter?.getOutputTensor(0)
            Log.d(TAG, "Output Type: ${outputTensor?.dataType()}, Shape: ${outputTensor?.shape()?.contentToString()}")

        } catch (e: Exception) {
            Log.e(TAG, "Erreur chargement modèle: ${e.message}")
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
        // Fallback mots-clés (très fiable pour la démo)
        val toxicKeywords = listOf("insulte", "violence", "haine", "drogue", "tuer", "mort", "porn", "sexe")
        if (toxicKeywords.any { text.contains(it, ignoreCase = true) }) {
            Log.d(TAG, "Toxicité détectée via mots-clés.")
            return 0.95f
        }

        if (interpreter == null) return 0.1f

        try {
            val inputTensor = interpreter!!.getInputTensor(0)
            val inputShape = inputTensor.shape() // [1, sequence_length]
            val seqLength = inputShape[1]

            val outputTensor = interpreter!!.getOutputTensor(0)
            val isOutputInt8 = outputTensor.dataType() == DataType.INT8

            // On prépare le buffer d'entrée (simulation de tokenisation simple)
            val inputBuffer = ByteBuffer.allocateDirect(seqLength * if (inputTensor.dataType() == DataType.INT8) 1 else 4)
            inputBuffer.order(ByteOrder.nativeOrder())

            // Simulation: on remplit avec des 0 ou des valeurs bidon (vrai tokeniseur requis en prod)
            for (i in 0 until seqLength) {
                if (inputTensor.dataType() == DataType.INT8) inputBuffer.put(0x01.toByte())
                else inputBuffer.putFloat(1.0f)
            }

            val outputShape = outputTensor.shape()
            val outputSize = if (outputShape.isEmpty()) 1 else outputShape.reduce { acc, i -> acc * i }

            val outputBuffer = ByteBuffer.allocateDirect(outputSize * if (isOutputInt8) 1 else 4)
            outputBuffer.order(ByteOrder.nativeOrder())

            interpreter!!.run(inputBuffer, outputBuffer)
            outputBuffer.rewind()

            return if (isOutputInt8) {
                (outputBuffer.get().toInt() and 0xFF) / 255.0f
            } else {
                outputBuffer.float
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'inférence : ${e.message}")
            return 0.1f
        }
    }
}
