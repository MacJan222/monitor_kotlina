package com.example.helloworld

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import com.github.mikephil.charting.data.*
import kotlinx.android.synthetic.main.sensor_screen.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class SensorScreen : Activity() {
    val displaySize = 300  // 15 seconds with 20Hz sampling
    val lpfFilterSize = 41

    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val date = Date()
    var current1 = ""
    var current2 = ""

    val lpf = FilteringLPF(lpfFilterSize, 20, 5, displaySize,
        10, 300, 6)

    lateinit var lineList: ArrayList<Entry>
    lateinit var lineDataSet: LineDataSet
    lateinit var lineData: LineData
    lateinit var peakList: ArrayList<Entry>
    lateinit var peakDataSet: ScatterDataSet
    lateinit var peakData: ScatterData
    var minMaxString: String? = null
//    var minMaxData: List<String>? = null
//    var minData: Float? = null
//    var maxData: Float? = null
    var combinedData = CombinedData()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sensor_screen)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        minMaxString = intent.getStringExtra("minMax")
//
//        minMaxString = minMaxString!!.replace("(", "")
//        minMaxString = minMaxString!!.replace(")", "")
//
//        minMaxData = minMaxString!!.split(",")
//        minData = minMaxData!![0].toFloat() - 50.0F
//        maxData = minMaxData!![1].toFloat() + 50.0F
        minMaxTextView.text=minMaxString

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

        sensorPlot.xAxis.setDrawGridLines(false)
        sensorPlot.axisLeft.setDrawGridLines(false)
        sensorPlot.axisRight.setEnabled(false)
        sensorPlot.axisLeft.labelPosition
        sensorPlot.axisRight.setDrawGridLines(false)
        sensorPlot.description.setEnabled(false)
        sensorPlot.axisLeft.isInverted = true
        sensorPlot.setBackgroundColor(Color.WHITE)
        sensorPlot.axisLeft.axisMaximum = 750F
        sensorPlot.axisLeft.axisMinimum = 100F

        for (i in 0 until displaySize) {
            lineDataSet.addEntry(Entry(i.toFloat(), 0.0F))
        }
        for (i in 0 until displaySize) {
            peakDataSet.addEntry(Entry(i.toFloat(), 0.0F))
        }

        combinedData.setData(lineData)
        combinedData.setData(peakData)
        sensorPlot.data = combinedData
        lineData.notifyDataChanged()
        peakData.notifyDataChanged()
        combinedData.notifyDataChanged()

        sensorPlot.invalidate()

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
    var lpfFillingTime = 0

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

            File(current1).appendText(rawSample.toString()+"\n")
            filteredSample = lpf.processLPF(rawSample.toInt())
            lpf.peakDetection()
            File(current2).appendText(filteredSample.toString()+"\n")

            for(i in 0 until lpf.filteredData.size) {
                lineDataSet.addEntry(Entry(i.toFloat(), lpf.filteredData[i]))
            }
            for(i in 0 until lpf.peakIndexesBot.size) {
                peakDataSet.addEntry(Entry(lpf.peakIndexesBot[i].toFloat(),  lpf.filteredData[lpf.peakIndexesBot[i]]))
            }

            combinedData.notifyDataChanged()
            sensorPlot.invalidate()

            handler.postDelayed(this, samplingFrequency.toLong())
        }
    }

        fun startBluetoothData() {
            handler.postDelayed(runnable, samplingFrequency.toLong())
        }

        fun stopBluetoothData() {
            handler.removeCallbacks(runnable)
        }
    }

//    fun getMinMax(): Pair<Float, Float> {
//        return Pair(arr.min(),arr.max())
//    }

