package com.example.bubu.GraphPlots

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.example.bubu.R
import com.example.bubu.TFLiteModelHelper
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.InputStreamReader
class MlGraphActivity : AppCompatActivity() {

    private lateinit var resultTextView: TextView
    private lateinit var rawDataText: TextView
    private lateinit var interpreter: Interpreter // Declare here

    private val activityMap = mapOf(
        0 to "sitting_dc",
        1 to "sitting_writing",
        2 to "standing_erase",
        3 to "standing_talking",
        4 to "sitting_typing",
        5 to "sitting_dw",
        6 to "standing_writing",
        7 to "standing_dc",
        8 to "standing_dw",
        9 to "sitting_talking",
        10 to "sitting_idle",
        11 to "sitting_scrolling"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ml_graph)

        resultTextView = findViewById(R.id.resultTextView)
        rawDataText = findViewById(R.id.rawDataText)

        // Load model and scaler asynchronously, then proceed
        TFLiteModelHelper.loadModel(this)
        TFLiteModelHelper.loadScaler(this)

        // Ensure the model is loaded before using interpreter
        try {
            interpreter = TFLiteModelHelper.fetchInterpreter() // Fetch after model load
        } catch (e: Exception) {
            Log.e("MlGraphActivity", "Interpreter not initialized: ${e.message}")
            resultTextView.text = "Interpreter not initialized. Try again."
            return
        }

        val uriString = intent.getStringExtra("URI") ?: return
        val uri = Uri.parse(uriString)

        // Log URI for debugging
        Log.d("MlGraphActivity", "URI: $uri")

        val (inputData, rawPreviewText) = loadAndPreprocessCsv(uri)

        rawDataText.text = rawPreviewText

        if (inputData.isNotEmpty()) {
            val predictedIndex = runInference(inputData)
            val predictedActivity = activityMap[predictedIndex] ?: "Unknown"
            resultTextView.text = "Predicted Activity: $predictedActivity"
        } else {
            resultTextView.text = "Failed to load or process CSV"
        }
    }


    private fun loadAndPreprocessCsv(uri: Uri): Pair<Array<FloatArray>, String> {
        val inputStream = contentResolver.openInputStream(uri) ?: return Pair(emptyArray(), "Failed to open file.")
        val reader = BufferedReader(InputStreamReader(inputStream))
        val processedData = mutableListOf<FloatArray>()
        val rawText = StringBuilder()

        var line = reader.readLine()
        if (line == null) {
            return Pair(emptyArray(), "CSV file is empty.")
        }

        rawText.append("CSV Header:\n$line\n\n")
        var count = 0

        reader.forEachLine {
            val parts = it.split(",")
            if (parts.size == 12) {
                try {
                    val floatRow = parts.map { it.toFloat() }
                    val standardizedRow = TFLiteModelHelper.standardizeRow(floatRow)
                    processedData.add(standardizedRow)

                    // Add preview (first 5 lines)
                    if (count < 5) {
                        rawText.append("Row ${count + 1}: $it\n")
                    }
                    count++
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        return Pair(processedData.toTypedArray(), rawText.toString())
    }

    private fun runInference(inputData: Array<FloatArray>): Int {
        val input = arrayOf(inputData) // Shape: [1, N, 12]
        val output = Array(1) { FloatArray(1) } // Output: [1, 1]

        interpreter.run(input, output)

        return output[0][0].toInt()
    }
}
