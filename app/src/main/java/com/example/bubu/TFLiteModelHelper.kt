package com.example.bubu

import android.content.Context
import android.util.Log
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.channels.FileChannel
import org.tensorflow.lite.flex.FlexDelegate

class TFLiteModelHelper(private val context: Context) {

    private lateinit var meanValues: FloatArray
    private lateinit var stdValues: FloatArray
    private lateinit var interpreter: Interpreter

    fun loadModel() {
        try {
            Log.d("TFLiteModelHelper", "Trying to load model...")

            val assetManager = context.assets
            val modelDescriptor = assetManager.openFd("lstm_model.tflite")
            Log.d("TFLiteModelHelper", "Model file descriptor obtained")

            val model = FileInputStream(modelDescriptor.fileDescriptor).channel.map(
                FileChannel.MapMode.READ_ONLY,
                modelDescriptor.startOffset,
                modelDescriptor.declaredLength
            )

            val options = Interpreter.Options().apply {
                addDelegate(FlexDelegate())
            }


//            val options = Interpreter.Options()
            interpreter = Interpreter(model, options)


            Log.d("TFLiteModelHelper", "Model loaded successfully")

        } catch (e: Exception) {
            Log.e("TFLiteModelHelper", "loadModel() failed: ${e.localizedMessage}", e)
            throw RuntimeException("Failed to load TFLite model: ${e.message}")
        }
    }

    fun fetchInterpreter(): Interpreter {
        if (!::interpreter.isInitialized) {
            throw IllegalStateException("Interpreter not initialized. Call loadModel() first.")
        }
        return interpreter
    }

    fun loadScaler() {
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
            throw IllegalStateException("Scaler not initialized. Call loadScaler() first.")
        }

        return FloatArray(row.size) { i ->
            (row[i] - meanValues[i]) / stdValues[i]
        }
    }
}

