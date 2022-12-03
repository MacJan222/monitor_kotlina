package com.example.helloworld

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import android.widget.TextView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.sensor_screen.*
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SensorScreen : Activity() {
    val DISPLAY = 100

    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val date = Date()
    var current = ""

    lateinit var lineList: ArrayList<Entry>
    lateinit var lineDataSet: LineDataSet
    lateinit var lineData: LineData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sensor_screen)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        lineList = ArrayList()
        lineDataSet = LineDataSet(lineList, "Dane z czujnika")
        lineDataSet.setDrawCircles(false)
        lineDataSet.setDrawValues(false)
        lineDataSet.setDrawHighlightIndicators(false)
        lineDataSet.color = Color.BLUE
        lineData = LineData(lineDataSet)
        sensorPlot.xAxis.setDrawGridLines(false)
        sensorPlot.axisLeft.setDrawGridLines(false)
        sensorPlot.axisRight.setEnabled(false)
        sensorPlot.axisLeft.labelPosition
        sensorPlot.axisRight.setDrawGridLines(false)
        sensorPlot.description.setEnabled(false)
        sensorPlot.xAxis.setDrawLabels(false)
        //sensorPlot.setViewPortOffsets(0f, 0f, 0f, 0f)
        sensorPlot.setVisibleXRangeMaximum(200F) //moze niepotrzebne
        sensorPlot.data = lineData

        val minMaxValues=intent.getStringExtra("minMax")
        minMaxTextView.text=minMaxValues

        //var sensorData: ArrayList<Float> = arrayListOf()

        btnStart.setOnClickListener {
//            sensorPlot.clear()
            btnStart.isEnabled = false
            startRandomData()
            val dirPath = baseContext.getExternalFilesDir(null).toString().removeSuffix("files")
            current = dirPath + formatter.format(date) + ".txt"
            //minMaxTextView.text = baseContext.getExternalFilesDir(null).toString()
        }

        btnEnd.setOnClickListener {
            stopRandomData()
            btnStart.isEnabled = true
            arr = arrayListOf()

        }


    }

    //zatrzymanie pobierania danych jesli wyjdziemy z tego okna
    override fun onDestroy() {
        super.onDestroy()
        stopRandomData()
    }


    var timer = Timer()
    var rnd = 0.0F
    var arr: MutableList<Float> = mutableListOf()
    val calibrationTime = DISPLAY

    var i = 0

    var monitor = object : TimerTask() {
        override fun run() {
            //setContentView(R.layout.sensor_screen)
            rnd=randomNumber()
            println(arr)
            File(current).appendText(rnd.toString()+"\n")
            //findViewById<TextView>(R.id.randomDataTextView).text=rnd.toString()
            this@SensorScreen.runOnUiThread {
                findViewById<TextView>(R.id.randomDataTextView).text = rnd.toString()
                if (arr.size > calibrationTime) {
                    lineDataSet.removeFirst()
                }
                if(rnd <= 20.0F && arr.isNotEmpty()) { lineDataSet.addEntry(Entry(i.toFloat(), arr.last())) }
                else { lineDataSet.addEntry(Entry(i.toFloat(), rnd)) }
                lineData.notifyDataChanged()
                sensorPlot.notifyDataSetChanged()
                sensorPlot.invalidate()

            }
            if(arr.size>calibrationTime){
                arr.removeAt(0)
            }
            if(rnd <= 20.0F && arr.isNotEmpty()){
                arr.add(arr.last())
            } else {
                arr.add(rnd)
            }

            i += 1

            //println(arr.size)
            //println(arr)
        }
    }

    fun startRandomData(){
        timer.schedule(monitor, 50, 50)

    }

    fun stopRandomData(){
        monitor.cancel()
        timer.cancel()

        i = 0

        monitor = object : TimerTask() {
            override fun run() {
                //setContentView(R.layout.sensor_screen)
                rnd=randomNumber()
                println(arr)
                File(current).appendText(rnd.toString()+"\n")
                //findViewById<TextView>(R.id.randomDataTextView).text=rnd.toString()
                this@SensorScreen.runOnUiThread {
                    findViewById<TextView>(R.id.randomDataTextView).text = rnd.toString()
                    if (arr.size > calibrationTime) {
                        lineDataSet.removeFirst()
                    }
                    if(rnd <= 20.0F && arr.isNotEmpty()) { lineDataSet.addEntry(Entry(i.toFloat(), arr.last())) }
                    else { lineDataSet.addEntry(Entry(i.toFloat(), rnd)) }
                    lineData.notifyDataChanged()
                    sensorPlot.notifyDataSetChanged()
                    sensorPlot.invalidate()

                }
                if(arr.size>calibrationTime){
                    arr.removeAt(0)
                }
                if(rnd <= 20.0F && arr.isNotEmpty()){
                    arr.add(arr.last())
                } else {
                    arr.add(rnd)
                }

                i += 1

                //println(arr.size)
                //println(arr)
            }
        }
        timer=Timer()
    }

    fun randomNumber(): Float {

        val input = BufferedReader(InputStreamReader(MainActivity.bluetoothSocket!!.getInputStream()))
        val rawData = input.readLine()

        var data = emptyList<String>()
        if(rawData.isNotEmpty()){
            data = rawData.split(" ")
        }

        var correctData = 0.0F

        if(data.size == 4 && data[0].isNotEmpty()) {
            correctData = data[0].toFloat()
        }
        return correctData
    }

    fun getMinMax(): Pair<Float, Float> {
        return Pair(arr.min(),arr.max())
    }

}