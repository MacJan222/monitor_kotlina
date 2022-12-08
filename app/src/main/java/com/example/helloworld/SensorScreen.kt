package com.example.helloworld

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.Toast
import com.github.mikephil.charting.data.*
import kotlinx.android.synthetic.main.sensor_screen.*
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*


class SensorScreen : Activity() {
    val displaySize = 300  // 15 seconds with 20Hz sampling
    val peakSearchSize = 10
    val lpfFilterSize = 41

    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val date = Date()
    var current1 = ""
    var current2 = ""

    val lpf = FilteringLPF(lpfFilterSize, 20, 5, displaySize,
        peakSearchSize, 6)

    lateinit var lineList: ArrayList<Entry>
    lateinit var lineDataSet: LineDataSet
    lateinit var lineData: LineData
    lateinit var peakList: ArrayList<Entry>
    lateinit var peakDataSet: ScatterDataSet
    lateinit var peakData: ScatterData
    var minMaxString: String? = null
    var minVal = 0.0F
    var maxVal = 750.0F

    var combinedData = CombinedData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sensor_screen)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        minMaxString = intent.getStringExtra("minMax")
        minMaxString = minMaxString!!.replace("(", "")
        minMaxString = minMaxString!!.replace(")", "")

        val minMaxData = minMaxString!!.split(",")
        minVal = minMaxData[0].toFloat()
        maxVal = minMaxData[1].toFloat()
        tvMinMax.text=minMaxString

        lineList = ArrayList()
        lineDataSet = LineDataSet(lineList, "Dane z czujnika")
        lineDataSet.setDrawCircles(false)
        lineDataSet.setDrawValues(false)
        lineDataSet.setDrawHighlightIndicators(false)
        lineDataSet.color = Color.RED
        lineData = LineData(lineDataSet)

        peakList = ArrayList()
        peakDataSet = ScatterDataSet(peakList, "Wykryte peaki")
        peakDataSet.setDrawValues(false)
        peakDataSet.setDrawHighlightIndicators(false)
        peakDataSet.color = Color.GREEN
        peakData = ScatterData(peakDataSet)

        ccSensorPlot.xAxis.setDrawGridLines(false)
        ccSensorPlot.axisLeft.setDrawGridLines(false)
        ccSensorPlot.axisRight.setEnabled(false)
        ccSensorPlot.axisRight.setDrawGridLines(false)
        ccSensorPlot.description.setEnabled(false)
        ccSensorPlot.setBackgroundColor(Color.WHITE)
        ccSensorPlot.axisLeft.axisMinimum = 0.0F
        ccSensorPlot.axisLeft.axisMaximum = 100F

        for (i in 0 until displaySize) {
            lineDataSet.addEntry(Entry(i.toFloat(), 0.0F))
        }
        for (i in 0 until displaySize) {
            peakDataSet.addEntry(Entry(i.toFloat(), 0.0F))
        }

        combinedData.setData(lineData)
        combinedData.setData(peakData)
        ccSensorPlot.data = combinedData
        lineData.notifyDataChanged()
        peakData.notifyDataChanged()
        combinedData.notifyDataChanged()

        ccSensorPlot.invalidate()

        btnStart.setOnClickListener {
            btnStart.isEnabled = false
            startBluetoothData()
            val dirPath = baseContext.getExternalFilesDir(null).toString().removeSuffix("files")
            current1 = dirPath + formatter.format(date) + "_raw.txt"
            current2 = dirPath + formatter.format(date) + "_filtered.txt"
        }

        btnEnd.setOnClickListener {
            stopBluetoothData()
            btnStart.isEnabled = true
            this.finish()
        }

    }

    //zatrzymanie pobierania danych jesli wyjdziemy z tego okna
    override fun onDestroy() {
        super.onDestroy()
        stopBluetoothData()
    }

    var rawSample = 9999.0F
    var filteredSample = 9999.0F
    val samplingFrequency = 50
    var rawBuffer = 0.0F
    var breathCount = 0
    var newEntry = 0.0F
    var scaledSample = 0.0F
    var is_new_peak = false

    val handler = Handler(Looper.getMainLooper())

    val runnable = object : Runnable {
        override fun run() {

            lineDataSet.clear()
            peakDataSet.clear()
            rawSample = readBluetoothData()

            if(rawSample == 9999.0F) {
                rawSample = rawBuffer
            }

            rawBuffer = rawSample
            randomDataTextView.text = rawSample.toString()

            filteredSample = lpf.processLPF(rawSample.toInt())

            scaledSample = 100 - (filteredSample - minVal) * 100 / (maxVal - minVal)
            if(scaledSample > 100) {
                scaledSample = 100F
            }
            if(scaledSample < 0) {
              scaledSample = 0F
            }
            lpf.peakDetection()

            for(i in 0 until lpf.filteredData.size) {
                newEntry = 100 - (lpf.filteredData[i] - minVal) * 100 / (maxVal - minVal)
                lineDataSet.addEntry(Entry(i.toFloat(), newEntry))
            }
            for(i in 0 until lpf.peakIndexesBot.size) {
                newEntry = 100 - (lpf.filteredData[lpf.peakIndexesBot[i]] - minVal) * 100 / (maxVal - minVal)
                peakDataSet.addEntry(Entry(lpf.peakIndexesBot[i].toFloat() + 1,  newEntry))
            }

            if(lpf.peakIndexesBot.isNotEmpty()) {
                is_new_peak = lpf.peakIndexesBot.last() == (displaySize - peakSearchSize / 2) - 1
                println(lpf.peakIndexesBot.last() - (displaySize - peakSearchSize / 2) + 1)
            }
            File(current2).appendText("$scaledSample,$is_new_peak\n")

            combinedData.notifyDataChanged()
            ccSensorPlot.invalidate()
            breathCount = lpf.peakIndexesBot.size - 1
            if (breathCount < 0){
                breathCount = 0
            }

            tvBreathCount.text = "Liczba oddechów na minutę: " + breathCount

            handler.postDelayed(this, samplingFrequency.toLong())
        }
    }

        fun startBluetoothData() {
            handler.postDelayed(runnable, samplingFrequency.toLong())
        }

        fun stopBluetoothData() {
            handler.removeCallbacks(runnable)
        }

    fun readBluetoothData(): Float {

        val input = BufferedReader(InputStreamReader(MainActivity.bluetoothSocket!!.getInputStream()))
        var rawData = ""
        try {
            rawData = input.readLine()
        }
        catch (e:java.lang.Exception) {
            Toast.makeText(this@SensorScreen, "Bluetooth nie działa!", Toast.LENGTH_SHORT).show()
        }
        var data = 9999.0F
        var correctData = 9999.0F

        if(rawData.isNotEmpty()){
            data = rawData.toFloat()
        }

        if(data > 100.0F) {
            correctData = data
        }
        return correctData
    }

}

//    fun getMinMax(): Pair<Float, Float> {
//        return Pair(arr.min(),arr.max())
//    }

