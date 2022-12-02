package com.example.helloworld

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import android.widget.TextView
import com.androidplot.xy.BoundaryMode
import com.androidplot.xy.LineAndPointFormatter
import com.androidplot.xy.SimpleXYSeries
import com.androidplot.xy.XYSeries
import kotlinx.android.synthetic.main.sensor_screen.*
import org.w3c.dom.Text
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sensor_screen)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        //lateinit var randomDataText = findViewById<TextView>(R.id.randomDataTextView)

        val minMaxValues=intent.getStringExtra("minMax")
        minMaxTextView.text=minMaxValues

        //BOUNDARIES DLA X
        sensorPlot.setDomainBoundaries(0, BoundaryMode.FIXED,DISPLAY,BoundaryMode.FIXED)

        //var sensorData: ArrayList<Float> = arrayListOf()

        btnStart.setOnClickListener {
            sensorPlot.clear()
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

    //WYKRES
    //val seriesFormat = LineAndPointFormatter(Color.BLUE, Color.BLACK,null,null)


    var timer = Timer()
    var rnd = 0.0F
    var arr: MutableList<Float> = mutableListOf()
    val calibrationTime = DISPLAY



    var monitor = object : TimerTask() {
        override fun run() {
            //setContentView(R.layout.sensor_screen)
            rnd=randomNumber()
            println(arr)
            File(current).appendText(rnd.toString()+"\n")
            //findViewById<TextView>(R.id.randomDataTextView).text=rnd.toString()
            this@SensorScreen.runOnUiThread(java.lang.Runnable {
                findViewById<TextView>(R.id.randomDataTextView).text=rnd.toString()
                val seriesFormat = LineAndPointFormatter(Color.BLUE, Color.BLACK,null,null)
                val series: XYSeries = SimpleXYSeries(arr, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "")
                findViewById<com.androidplot.xy.XYPlot>(R.id.sensorPlot).clear()
                findViewById<com.androidplot.xy.XYPlot>(R.id.sensorPlot).addSeries(series,seriesFormat)
                findViewById<com.androidplot.xy.XYPlot>(R.id.sensorPlot).redraw()
            })
            if(arr.size>calibrationTime){
                arr.removeAt(0)
            }
            if(rnd == 0.0F && arr.isNotEmpty()){
                arr.add(arr.last())
            } else {
                arr.add(rnd)
            }

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

        monitor = object : TimerTask() {
            override fun run() {
                //setContentView(R.layout.sensor_screen)
                rnd=randomNumber()
                println(rnd)
                //findViewById<TextView>(R.id.randomDataTextView).text=rnd.toString()
                this@SensorScreen.runOnUiThread(java.lang.Runnable {
                    findViewById<TextView>(R.id.randomDataTextView).text=rnd.toString()
                    val seriesFormat = LineAndPointFormatter(Color.BLUE, Color.BLACK,null,null)
                    val series: XYSeries = SimpleXYSeries(arr, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "")
                    findViewById<com.androidplot.xy.XYPlot>(R.id.sensorPlot).clear()
                    findViewById<com.androidplot.xy.XYPlot>(R.id.sensorPlot).addSeries(series,seriesFormat)
                    findViewById<com.androidplot.xy.XYPlot>(R.id.sensorPlot).redraw()
                })
                if(arr.size>calibrationTime){
                    arr.removeAt(0)
                }
                if(rnd == 0.0F && arr.isNotEmpty()){
                    arr.add(arr.last())
                } else {
                    arr.add(rnd)
                }
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