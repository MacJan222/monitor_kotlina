package com.example.helloworld

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import com.androidplot.xy.BoundaryMode
import com.androidplot.xy.LineAndPointFormatter
import com.androidplot.xy.SimpleXYSeries
import com.androidplot.xy.XYSeries
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
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

    lateinit var lineList: ArrayList<Entry>
    lateinit var lineDataSet: LineDataSet
    lateinit var lineData: LineData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sensor_screen)

        lineList = ArrayList()
        //lineList.add(Entry(0.0f,0.0f))
        lineDataSet = LineDataSet(lineList, "Dane z czujnika")
        lineDataSet.setDrawCircles(false)
        lineDataSet.setDrawValues(false)
        lineDataSet.setDrawHighlightIndicators(false)
        lineDataSet.setColor(Color.BLUE)
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

        //sensorPlot.
        //lineDataSet.setCircleColors(*ColorTemplate.)

        //lateinit var randomDataText = findViewById<TextView>(R.id.randomDataTextView)

        val minMaxValues=intent.getStringExtra("minMax")
        minMaxTextView.text=minMaxValues


        //var sensorData: ArrayList<Float> = arrayListOf()

        btnStart.setOnClickListener {

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

    var i = 0

    var monitor = object : TimerTask() {
        override fun run() {
            //setContentView(R.layout.sensor_screen)
            rnd=randomNumber()
            File(current).appendText(rnd.toString()+"\n")
            //findViewById<TextView>(R.id.randomDataTextView).text=rnd.toString()
            this@SensorScreen.runOnUiThread(java.lang.Runnable {
                findViewById<TextView>(R.id.randomDataTextView).text=rnd.toString()

                if(arr.size>calibrationTime){
                    lineDataSet.removeFirst()
                }

                lineDataSet.addEntry(Entry(i.toFloat(),rnd))
                lineData.notifyDataChanged()
                sensorPlot.notifyDataSetChanged()
                sensorPlot.invalidate()
            })
            if(arr.size>calibrationTime){
                arr.removeAt(0)
            }
            arr+=rnd
            i+=1

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