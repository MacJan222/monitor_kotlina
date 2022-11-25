package com.example.helloworld

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import com.androidplot.xy.BoundaryMode
import com.androidplot.xy.LineAndPointFormatter
import com.androidplot.xy.SimpleXYSeries
import com.androidplot.xy.XYSeries
import kotlinx.android.synthetic.main.sensor_screen.*
import org.w3c.dom.Text
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SensorScreen : Activity() {
    val DISPLAY = 200

    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val date = Date()
    var current = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sensor_screen)

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
    var arr: ArrayList<Float> = arrayListOf()
    val calibrationTime = DISPLAY



    var monitor = object : TimerTask() {
        override fun run() {
            //setContentView(R.layout.sensor_screen)
            rnd=randomNumber()
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
            arr+=rnd
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
                arr+=rnd
                //println(arr.size)
                //println(arr)
            }
        }
        timer=Timer()
    }

    fun randomNumber(): Float {
        return Random().nextFloat() * (600 - 100) + 100
    }

    fun getMinMax(): Pair<Float, Float> {
        return Pair(arr.min(),arr.max())
    }

}