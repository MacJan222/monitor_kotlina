package com.example.helloworld

import android.R
import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ToggleButton
import kotlinx.android.synthetic.main.activity_main.*


class DummyData {

    var calibrationData: ArrayList<Float> = arrayListOf()
    val calibrationTime = 1200
    var rawBuffer = 9999.0F
    var rawSample = 9999.0F
    var filteredSample = 9999.0F
    val samplingFrequency = 50


    val lpfCalibration = FilteringLPF(41, 20, 5, 1200,
        10, 1200, 6)

    val handler = Handler(Looper.getMainLooper())

    val runnable = object : Runnable {
        override fun run() {
            if(calibrationData.size < 41) {
                // todo: connect button to main activity
//                (baseContext).findViewById<View>(R.id.sensorCalibrate) as ToggleButton
            }

            rawSample = readBluetoothData()

            if(rawSample == 9999.0F) {
                rawSample = rawBuffer
            }

            rawBuffer = rawSample
            filteredSample = lpfCalibration.processLPF(rawSample.toInt())
            calibrationData += filteredSample

            handler.postDelayed(this, samplingFrequency.toLong())
        }
    }

    fun startRandomData(){
        handler.postDelayed(runnable, samplingFrequency.toLong())
    }

    fun stopRandomData(){
        handler.removeCallbacks(runnable)
    }

    fun getMinMax(): Pair<Float, Float> {
        val slicedArr = calibrationData.slice(calibrationData.size - 41 until calibrationData.size)
        return Pair(slicedArr.min(), slicedArr.max())
    }

}

