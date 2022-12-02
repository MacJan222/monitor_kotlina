package com.example.helloworld

import android.bluetooth.BluetoothSocket
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import kotlin.collections.ArrayList

class DummyData() {

    var timer = Timer()
    //var rnd = 0.0F
    var arr: ArrayList<Float> = arrayListOf()
    val calibrationTime = 100

    var monitor = object : TimerTask() {
        override fun run() {

            val input = BufferedReader(InputStreamReader(MainActivity.bluetoothSocket!!.getInputStream()))
            val rawData = input.readLine()
            if(arr.size>calibrationTime){
                arr.removeAt(0)
            }

            var data = emptyList<String>()
            if(rawData.isNotEmpty()){
                data = rawData.split(" ")
            }

            if(data.size == 4 && data[0].isNotEmpty()) {
                arr+=data[0].toFloat()
            }
            println(arr.size)
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

                val input = BufferedReader(InputStreamReader(MainActivity.bluetoothSocket!!.getInputStream()))
                val rawData = input.readLine()
                if(arr.size>calibrationTime){
                    arr.removeAt(0)
                }

                var data = emptyList<String>()
                if(rawData.isNotEmpty()){
                    data = rawData.split(" ")
                }

                if(data.size == 4 && data[0].isNotEmpty()) {
                    arr+=data[0].toFloat()
                }
                println(arr.size)
            }
        }

        timer=Timer()
    }

    fun resetData() {
        arr = arrayListOf()
    }

    fun getMinMax(): Pair<Float, Float> {
        return Pair(arr.min(),arr.max())
    }

}

