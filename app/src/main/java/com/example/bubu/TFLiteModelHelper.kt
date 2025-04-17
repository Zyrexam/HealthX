package com.example.bubu

import android.content.Context
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream

import java.io.InputStreamReader
import java.nio.channels.FileChannel

object TFLiteModelHelper {
    lateinit var interpreter: Interpreter
    private lateinit var meanValues: FloatArray
    private lateinit var stdValues: FloatArray

    fun loadModel(context: Context) {
        try {
            val modelFile = "lstm_model.tflite"
            val fileDescriptor = context.assets.openFd(modelFile)
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            val modelBuffer = fileChannel.map(
                FileChannel.MapMode.READ_ONLY,
                startOffset,
                declaredLength
            )

            interpreter = Interpreter(modelBuffer)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun fetchInterpreter(): Interpreter {
        if (!::interpreter.isInitialized) {
            throw IllegalStateException("Interpreter not initialized. Call loadModel(context) first.")
        }
        return interpreter
    }


    fun loadScaler(context: Context) {
        try {
            val scalerFile = "scaler.json"
            context.assets.open(scalerFile).use { stream ->
                val jsonText = InputStreamReader(stream).readText()
                val obj = JSONObject(jsonText)

                val meanArray = obj.getJSONArray("mean")
                val stdArray = obj.getJSONArray("scale")

                meanValues = FloatArray(meanArray.length())
                stdValues = FloatArray(stdArray.length())

                for (i in 0 until meanArray.length()) {
                    meanValues[i] = meanArray.getDouble(i).toFloat()
                    stdValues[i] = stdArray.getDouble(i).toFloat()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Failed to load scaler: ${e.message}")
        }
    }

    fun standardizeRow(row: List<Float>): FloatArray {
        if (!::meanValues.isInitialized || !::stdValues.isInitialized) {
            throw IllegalStateException("Scaler not initialized. Call loadScaler(context) first.")
        }

        return FloatArray(row.size) { i ->
            (row[i] - meanValues[i]) / stdValues[i]
        }
    }
}
