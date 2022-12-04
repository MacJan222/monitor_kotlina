package com.example.helloworld

import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import kotlin.collections.ArrayList

class DummyData {

    var timer = Timer()
    var singleData: Float? = 0.0F
    var arr: ArrayList<Float> = arrayListOf()
    val calibrationTime = 1200
    var last_sample = 300.0F

    val lpfCalibration = FilteringLPF(41, 20, 5, 1200,
        10, 1200, 6)

    var monitor = object : TimerTask() {
        override fun run() {
            singleData = readBluetoothData(last_sample = last_sample)

            if(arr.isNotEmpty() && singleData != null) {
                singleData = lpfCalibration.processLPF(singleData!!.toInt())
            }

            if(arr.size > calibrationTime){ arr.removeAt(0) }

            if(arr.isNotEmpty()) {
                if (singleData == null) {
                    arr.add(arr.last())
                } else {
                    arr.add(singleData!!)
                }
            }
            else {

                arr.add(300.0F)  // UWAGA HARDKODOWANE TODO: POPRAWIC
            }

            println(arr)
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
                singleData = readBluetoothData(last_sample = last_sample)

                if(arr.isNotEmpty() && singleData != null) {
                    singleData = lpfCalibration.processLPF(singleData!!.toInt())
                }

                if(arr.size > calibrationTime){ arr.removeAt(0) }

                if(arr.isNotEmpty()) {
                    if (singleData == null) {
                        arr.add(arr.last())
                    } else {
                        arr.add(singleData!!)
                    }
                }
                else {
                    arr.add(300.0F)  // UWAGA HARDKODOWANE TODO: POPRAWIC
                }

                println(arr)
                if (singleData == null) {
                    if(arr.isNotEmpty()) {
                        last_sample = arr.last()
                    }
                    else {
                        last_sample = 300.0F
                    }
                } else {
                    last_sample = singleData!!
                }

            }
        }

        timer=Timer()
    }

    fun resetData() {
        arr = arrayListOf()
    }

    fun getMinMax(): Pair<Float, Float> {
        val sliced_arr = arr.slice(arr.size - 41 until arr.size)

        return if(sliced_arr.min() < 80) {
            Pair(80.0F, sliced_arr.max())
        } else {
            Pair(sliced_arr.min(), sliced_arr.max())
        }
    }

}

