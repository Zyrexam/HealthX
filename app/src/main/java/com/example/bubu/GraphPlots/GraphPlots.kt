package com.example.bubu.GraphPlots

import android.content.ContentValues
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import com.example.bubu.R
import com.example.bubu.dataClass
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlin.math.abs


class graphPlots : AppCompatActivity() {
    var measurments:List<dataClass> = listOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph_plots)

        val uri:String? = intent.getStringExtra("URI")

//        findViewById<TextView>(R.id.textView).setText(uri)

        uri?.let {
            // Convert the string back to a URI
            val fileUri = Uri.parse(it)

            // Now you can use the URI to access the file
            // For example, you can read the file content here
            readTextFromUri(fileUri)
        }

        val graph = findViewById<GraphView>(R.id.graph)



        val seriesX = LineGraphSeries<DataPoint>()
        val seriesY = LineGraphSeries<DataPoint>()
        val seriesZ = LineGraphSeries<DataPoint>()



        // Populate the series with data
        measurments.forEach { measurement ->
//            Log.d(TAG,"Measurments: ${measurement}")
            seriesX.appendData(DataPoint(measurement.tAndroid.toDouble()/1000000000, measurement.x), true, measurments.size)
            seriesY.appendData(DataPoint(measurement.tAndroid.toDouble()/1000000000, measurement.y), true, measurments.size)
            seriesZ.appendData(DataPoint(measurement.tAndroid.toDouble()/1000000000, measurement.z), true, measurments.size)
        }

        Log.d("Measurments Size: ","${measurments.size}")
//        Log.d(TAG,"Data Class Size: ${}")

        seriesX.title = "X"
        seriesX.color = Color.RED
        seriesY.title = "Y"
        seriesY.color = Color.GREEN
        seriesZ.title = "Z"
        seriesZ.color = Color.BLUE

        seriesX.isDrawDataPoints = true  // Optionally, to draw individual data points
        seriesX.dataPointsRadius = 5f   // Adjust the radius of data points for visibility
        seriesX.thickness = 4          // Adjust the thickness of the line

// To set the line to be a bit smoother visually (though it doesn't change the actual data points)
        seriesX.setAnimated(true)

        seriesY.isDrawDataPoints = true  // Optionally, to draw individual data points
        seriesY.dataPointsRadius = 5f   // Adjust the radius of data points for visibility
        seriesY.thickness = 4           // Adjust the thickness of the line

// To set the line to be a bit smoother visually (though it doesn't change the actual data points)
        seriesY.setAnimated(true)

        seriesZ.isDrawDataPoints = true  // Optionally, to draw individual data points
        seriesZ.dataPointsRadius = 5f   // Adjust the radius of data points for visibility
        seriesZ.thickness = 4           // Adjust the thickness of the line

// To set the line to be a bit smoother visually (though it doesn't change the actual data points)
        seriesZ.setAnimated(true)

        graph.addSeries(seriesX)
        graph.addSeries(seriesY)
        graph.addSeries(seriesZ)

        // Optional: customize viewport
        graph.viewport.isScalable = true
        graph.viewport.setScalableY(true)

        graph.viewport.isXAxisBoundsManual = true
        graph.viewport.setMinX(seriesX.lowestValueX)
        graph.viewport.setMaxX(seriesX.highestValueX)

// Optionally, set manual Y bounds
        graph.viewport.isYAxisBoundsManual = true

        graph.viewport.isScalable = true
        graph.viewport.isScrollable = true

// For scaling and scrolling on both axes
        graph.viewport.setScalableY(true)
        graph.viewport.setScrollableY(true)

        val maxMeasurements = findMaxMeasurementsWithAdjustedTimeAndNegativeSign(measurments)

//        maxMeasurements.forEach {
//            println("Adjusted tAndroid: ${it.first}, Max Dimension: ${it.second}, Value: ${it.third}")
//        }
        val intervals = findContinuousIntervalsWithSameMaxDimension(maxMeasurements)

        intervals.forEach {
            println("From ${it[0]}s to ${it[1]}s, Max Dimension: ${it[2]}, Occurrences: ${it[3]}")
        }



        // Assuming you have a TextView with the ID textView in your layout
        val textView: TextView = findViewById(R.id.textView)
        var currentTime = 0.0
        val maxTime = intervals.last()[1] as Double

        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                // Find the current interval based on currentTime
                val currentInterval = intervals.find { interval ->
                    currentTime >= interval[0] as Double && currentTime <= interval[1] as Double
                }

                // Update the TextView with the current max dimension
                currentInterval?.let {
                    var text:String  ="Time: $currentTime, Rotaion: "
                    if(it[2] == "x"){
                        text += "Forward"
                    }
                    if(it[2] == "-x"){
                        text += "Backward"
                    }
                    if(it[2] == "y"){
                        text += "Left"
                    }
                    if(it[2] == "-y"){
                        text += "Right"
                    }
                    if(it[2] == "z"){
                        text += "Clockwise"
                    }
                    if(it[2] == "-z"){
                        text += "Anticlockwise"
                    }

                    textView.text = text
                }

                if (currentTime <= maxTime) {
                    currentTime++
                    handler.postDelayed(this, 1000) // Update every second
                }
            }
        }

// Start the timer
        handler.post(runnable)


    }

    private fun findContinuousIntervalsWithSameMaxDimension(maxMeasurements: List<Triple<Double, String, Double>>): List<List<Any>> {
        // Result list to store the intervals
        val intervals = mutableListOf<List<Any>>()

        // Temporary variables to track the current interval
        var currentStart = maxMeasurements.first().first
        var currentDimension = maxMeasurements.first().second
        var count = 1

        maxMeasurements.forEachIndexed { index, measurement ->
            // For the first element, just initialize the variables
            if (index == 0) return@forEachIndexed

            // If the current dimension is the same as the previous one, we extend the interval
            if (measurement.second == currentDimension) {
                count++
                // If it's the last measurement, we need to add the interval to the result list
                if (index == maxMeasurements.size - 1) {
                    intervals.add(listOf(currentStart, measurement.first, currentDimension, count))
                }
            } else {
                // Dimension changed, save the previous interval and start a new one
                intervals.add(listOf(currentStart, maxMeasurements[index - 1].first, currentDimension, count))
                currentStart = measurement.first
                currentDimension = measurement.second
                count = 1
            }
        }

        // Handle the case where all dimensions are the same by adding the interval if the result list is empty
        if (intervals.isEmpty()) {
            intervals.add(listOf(currentStart, maxMeasurements.last().first, currentDimension, count))
        }

        return intervals
    }


    fun findMaxMeasurementsWithAdjustedTimeAndNegativeSign(measurements: List<dataClass>): List<Triple<Double, String, Double>> {
        return measurements.map { measurement ->
            // Adjust tAndroid by dividing by 1,000,000,000
            val adjustedTime = measurement.tAndroid / 1_000_000_000.0

            // Determine the maximum value and its dimension, with a sign adjustment for negative values
            val maxInfo = listOf(
                Triple("x", measurement.x, abs(measurement.x)),
                Triple("y", measurement.y, abs(measurement.y)),
                Triple("z", measurement.z, abs(measurement.z))
            ).maxByOrNull { it.third }!!

            // Determine the dimension string, including the negative sign if applicable
            val dimensionWithSign = if (maxInfo.second < 0) "-${maxInfo.first}" else maxInfo.first

            // The maximum magnitude value (always positive)
            val maxValue = maxInfo.third

            Triple(adjustedTime, dimensionWithSign, maxValue)
        }
    }

    private fun readTextFromUri(uri: Uri) {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            val text = inputStream.bufferedReader().use {
                it.readText()
            }
            measurments = parseGyroscopeDataWithAdjustedTimestamps(text)

            Log.d(ContentValues.TAG,"Measurment Size: ${measurments.size} Text Size: ${text.length}")

            // Do something with the text content
        }
    }

    fun parseGyroscopeDataWithAdjustedTimestamps(rawData: String): List<dataClass> {
        val measurements = mutableListOf<dataClass>()
        val lines = rawData.split("\n")

        var firstTimestampAndroid: Long? = null

        for (line in lines.drop(1)) { // Skip header
            if (line.isBlank()) continue // Skip blank lines

            val parts = line.split(";")
            if (parts.size < 6) continue // Skip lines with insufficient data

            try {
                // Parse timestamps and data from the current line
                val tAndroid = parts[0].toLong()
                val tUnix = parts[1].toLong()
                val x = parts[2].toDouble()
                val y = parts[3].toDouble()
                val z = parts[4].toDouble()
                val a = parts[5].toInt()

                // Initialize the first timestamp if not already set
                if (firstTimestampAndroid == null) {
                    firstTimestampAndroid = tAndroid
                }

                // Adjust timestamps relative to the first timestamp
                val adjustedTAndroid = tAndroid - firstTimestampAndroid

                // Create and add the measurement with the adjusted timestamp
                val measurement = dataClass(
                    tAndroid = adjustedTAndroid,
                    tUnix = tUnix, // If you want to adjust tUnix as well, you'd need to handle it similarly
                    x = x,
                    y = y,
                    z = z,
                    a = a
                )
                measurements.add(measurement)
            } catch (e: NumberFormatException) {
                // Handle potential format exceptions, e.g., skip malformed lines
                continue
            }
        }

        return measurements
    }
}