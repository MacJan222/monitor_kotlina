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
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SensorScreen : Activity() {
    val DISPLAY = 300  // 15 seconds with 20Hz sampling

    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val date = Date()
    var current1 = ""
    var current2 = ""

    val lpf = FilteringLPF(41, 20, 5, 1200,
        10, 1200, 6)

    lateinit var lineList: ArrayList<Entry>
    lateinit var lineDataSet: LineDataSet
    lateinit var lineData: LineData
    var minMaxString: String? = null
    var minMaxData: List<String>? = null
    var minData: Float? = null
    var maxData: Float? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sensor_screen)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        minMaxString = intent.getStringExtra("minMax")

        minMaxString = minMaxString!!.replace("(", "")
        minMaxString = minMaxString!!.replace(")", "")

        minMaxData = minMaxString!!.split(",")
        minData = minMaxData!![0].toFloat() - 50.0F
        maxData = minMaxData!![1].toFloat() + 50.0F
        minMaxTextView.text=minMaxString

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
        sensorPlot.axisLeft.isInverted = true
        //sensorPlot.setViewPortOffsets(0f, 0f, 0f, 0f)
//        sensorPlot.setVisibleXRangeMaximum(200F) //moze niepotrzebne
        sensorPlot.axisLeft.axisMinimum = this.minData!!
        sensorPlot.axisLeft.axisMaximum = this.maxData!!
        sensorPlot.data = lineData





        //var sensorData: ArrayList<Float> = arrayListOf()

        btnStart.setOnClickListener {
//            sensorPlot.clear()
            btnStart.isEnabled = false
            startRandomData()
            val dirPath = baseContext.getExternalFilesDir(null).toString().removeSuffix("files")
            current1 = dirPath + formatter.format(date) + "_raw.txt"
            current2 = dirPath + formatter.format(date) + "_filtered.txt"
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
    var rnd: Float? = null
    var arr: MutableList<Float?> = mutableListOf()
    val calibrationTime = DISPLAY

    var i = 0
    var last_sample = 300.0F
    var tmp = 300.0F

    var monitor = object : TimerTask() {
        override fun run() {

            rnd = readBluetoothData(last_sample = last_sample)
            println(arr)

//            if(i == 41) {
//                tmp = arr.last()!!  // warning not safe
//                arr.clear()
//                arr.add(tmp)
//            }

            this@SensorScreen.runOnUiThread {
                findViewById<TextView>(R.id.randomDataTextView).text = rnd.toString()
                if (arr.size > calibrationTime) {
                    lineDataSet.removeFirst()
                }

                if(arr.isNotEmpty()) {
                    if (rnd == null) {
                        rnd = arr.last()
                    }
                    File(current1).appendText(rnd.toString()+"\n")
                    rnd = lpf.processLPF(rnd!!.toInt())
                    File(current2).appendText(rnd.toString()+"\n")
                    lpf.peakDetection()
                    if(i > 41) {
                        lineDataSet.addEntry(Entry(i.toFloat(), rnd!!))
                    }
                }

                lineData.notifyDataChanged()
                sensorPlot.notifyDataSetChanged()
                sensorPlot.invalidate()
            }
            if(arr.size>calibrationTime){
                arr.removeAt(0)
            }

            if (arr.isEmpty()) {
                if (rnd == null) {
                    arr.add(minData!!)
                } else {
                    arr.add(rnd)
                }
            }
            else {
                if (rnd == null) {
                    arr.add(arr.last())
                }
                else {
                    arr.add(rnd)
                }
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

                rnd = readBluetoothData(last_sample = last_sample)
                println(arr)

//                if(i == 41) {
//                    tmp = arr.last()!!  // warning not safe
//                    arr.clear()
//                    arr.add(tmp)
//                }

                this@SensorScreen.runOnUiThread {
                    findViewById<TextView>(R.id.randomDataTextView).text = rnd.toString()
                    if (arr.size > calibrationTime) {
                        lineDataSet.removeFirst()
                    }

                    if(arr.isNotEmpty()) {
                        if (rnd == null) {
                            rnd = arr.last()
                        }
                        File(current1).appendText(rnd.toString()+"\n")
                        rnd = lpf.processLPF(rnd!!.toInt())
                        File(current2).appendText(rnd.toString()+"\n")
                        if(i > 41) {
                            lpf.peakDetection()
                            lineDataSet.addEntry(Entry(i.toFloat(), rnd!!))
                        }
                    }

                    lineData.notifyDataChanged()
                    sensorPlot.notifyDataSetChanged()
                    sensorPlot.invalidate()
                }

                if(arr.size>calibrationTime){
                    arr.removeAt(0)
                }

                if (arr.isEmpty()) {
                    if (rnd == null) {
                        arr.add(minData!!)
                    } else {
                        arr.add(rnd)
                    }
                }
                else {
                    if (rnd == null) {
                        arr.add(arr.last())
                    }
                    else {
                        arr.add(rnd)
                    }
                }

                i += 1

                //println(arr.size)
                //println(arr)
            }
        }
        timer=Timer()
    }



//    fun getMinMax(): Pair<Float, Float> {
//        return Pair(arr.min(),arr.max())
//    }

}