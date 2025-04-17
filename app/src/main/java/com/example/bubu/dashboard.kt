package com.example.bubu

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.firebase.auth.FirebaseAuth
import com.jjoe64.graphview.GraphView
import java.util.concurrent.TimeUnit
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
//import java.lang.reflect.Field
import java.util.Calendar
import java.util.Date
import com.google.android.gms.fitness.data.Field
import com.jjoe64.graphview.DefaultLabelFormatter
import java.util.TimeZone


class dashboard : AppCompatActivity() {
    val fitnessOptions: FitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .build()

    val fitnessOptions1 = FitnessOptions.builder()
        .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)



        val email:String = FirebaseAuth.getInstance().currentUser?.email.toString()
        val UID:String = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val username:String = FirebaseAuth.getInstance().currentUser?.displayName.toString()

        findViewById<TextView>(R.id.email).setText(email)
        findViewById<TextView>(R.id.UID).setText(UID)
        findViewById<TextView>(R.id.username).setText(username)

        logTodayStepCount()

//        checkGoogleFitPermissions()
    plotTodaysStepCountsHourByHour()
        plotEnergyExpendedToday()
    }

    private fun plotEnergyExpendedToday() {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.time = Date()
        val endTime = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis

        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_CALORIES_EXPENDED)
            .bucketByTime(1, TimeUnit.HOURS)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(this, GoogleSignIn.getAccountForExtension(this, fitnessOptions))
            .readData(readRequest)
            .addOnSuccessListener { response ->
                val series = LineGraphSeries<DataPoint>()
                for (bucket in response.buckets) {
                    val totalCalories = bucket.dataSets.firstOrNull()?.dataPoints?.firstOrNull()?.getValue(Field.FIELD_CALORIES)?.asFloat() ?: 0f
                    // Correctly accessing the start time of the bucket
                    val hourOfDay = TimeUnit.MILLISECONDS.toHours(bucket.getStartTime(TimeUnit.MILLISECONDS) - startTime)
                    series.appendData(DataPoint(hourOfDay.toDouble(), totalCalories.toDouble()), true, 24)
                }

                val graph = findViewById<GraphView>(R.id.graph)
                graph.addSeries(series)

                graph.viewport.isXAxisBoundsManual = true
                graph.viewport.setMinX(0.0) // Assuming your day starts at hour 0
                graph.viewport.setMaxX(24.0) // Assuming you want to display up to hour 24

// Optionally, set manual Y bounds if you also want to control the Y axis
                graph.viewport.isYAxisBoundsManual = true
                graph.viewport.setMinY(0.0) // For example, starting at 0 steps
                graph.viewport.setMaxY(series.highestValueY + 100) // You might want to add a buffer or set a specific max

// Enable scrolling and scaling
                graph.viewport.isScrollable = true
                graph.viewport.isScalable = true

                // Set the title for the horizontal (X) axis
                graph.gridLabelRenderer.horizontalAxisTitle = "Hours of the Day"

// Set the title for the vertical (Y) axis
                graph.gridLabelRenderer.verticalAxisTitle = "Energy Expended"

                // Further configuration for the graph...
            }
            .addOnFailureListener { e ->
                Log.e("EnergyExpended", "There was a problem getting the energy expended.", e)
            }
    }


    private fun checkGoogleFitPermissions() {
        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            Log.d(TAG,"Dikkat hai")
            GoogleSignIn.requestPermissions(
                this,
                REQUEST_OAUTH_REQUEST_CODE,
                account,
                fitnessOptions
            )
        } else {
            accessGoogleFit()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                accessGoogleFit()
            }
        }
    }

    private fun plotTodaysStepCountsHourByHour() {
        val cal = Calendar.getInstance()
        cal.time = Date()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startTime = cal.timeInMillis
        val endTime = System.currentTimeMillis()

        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
            .bucketByTime(1, TimeUnit.HOURS)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(this, GoogleSignIn.getAccountForExtension(this, fitnessOptions))
            .readData(readRequest)
            .addOnSuccessListener { response ->
                val series = LineGraphSeries<DataPoint>()
                for (bucket in response.buckets) {
                    // Inside this loop, you can access bucket.startTimeMillis
                    val totalSteps = bucket.dataSets.firstOrNull()?.dataPoints?.firstOrNull()?.getValue(Field.FIELD_STEPS)?.asInt() ?: 0
                    Log.d(TAG,"Steps: $totalSteps")
                    val hourOfDay = TimeUnit.MILLISECONDS.toHours(bucket.getStartTime(TimeUnit.MILLISECONDS) - startTime)
                    Log.d(TAG,"Time: $hourOfDay")
                    series.appendData(DataPoint(hourOfDay.toDouble(), totalSteps.toDouble()), true, 24)
//                    Log.d(TAG,"Chla")
                }

                val graph = findViewById<GraphView>(R.id.grap)

                graph.addSeries(series)

                // Set manual X bounds
                graph.viewport.isXAxisBoundsManual = true
                graph.viewport.setMinX(0.0) // Assuming your day starts at hour 0
                graph.viewport.setMaxX(24.0) // Assuming you want to display up to hour 24

// Optionally, set manual Y bounds if you also want to control the Y axis
                graph.viewport.isYAxisBoundsManual = true
                graph.viewport.setMinY(0.0) // For example, starting at 0 steps
                graph.viewport.setMaxY(series.highestValueY + 100) // You might want to add a buffer or set a specific max

// Enable scrolling and scaling
                graph.viewport.isScrollable = true
                graph.viewport.isScalable = true

                graph.gridLabelRenderer.horizontalAxisTitle = "Hours of the Day"

// Set the title for the vertical (Y) axis
                graph.gridLabelRenderer.verticalAxisTitle = "Energy Expended"
            }
            .addOnFailureListener { e ->
                Log.e("StepCountByHour", "There was a problem getting the step counts.", e)
            }
    }

    private fun logTodayStepCount() {
        val cal = Calendar.getInstance()
        cal.time = Date()
        val endTime = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startTime = cal.timeInMillis

        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(this, GoogleSignIn.getAccountForExtension(this, fitnessOptions))
            .readData(readRequest)
            .addOnSuccessListener { response ->
                val bucket = response.buckets.firstOrNull()
                val dataSet = bucket?.dataSets?.firstOrNull()
                val totalSteps = dataSet?.dataPoints?.firstOrNull()?.getValue(DataType.AGGREGATE_STEP_COUNT_DELTA.fields[0])?.asInt()

                Log.d("TodayStepCount", "Steps today: $totalSteps")
            }
            .addOnFailureListener { e ->
                Log.e("TodayStepCount", "There was a problem getting the step count.", e)
            }
    }

    private fun accessGoogleFit() {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.DAYS.toMillis(10)

        Fitness.getHistoryClient(this, GoogleSignIn.getAccountForExtension(this, fitnessOptions))
            .readDailyTotalFromLocalDevice(DataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener { dataSet ->
                val totalSteps = if (dataSet.isEmpty) 0 else dataSet.dataPoints.first().getValue(DataType.AGGREGATE_STEP_COUNT_DELTA.fields[0]).asInt()
                val graph = findViewById<GraphView>(R.id.grap) // Ensure ID matches your layout

                // Creating a single DataPoint array explicitly
                val dataPoints: Array<DataPoint> = arrayOf(DataPoint(0.0, totalSteps.toDouble()))
                val series = LineGraphSeries<DataPoint>(dataPoints) // Explicit type declaration

                graph.addSeries(series)
            }
            .addOnFailureListener { e ->
                Log.e("GoogleFit", "There was a problem getting the step count.", e)
            }
    }


    companion object {
        private const val REQUEST_OAUTH_REQUEST_CODE = 1
    }
}