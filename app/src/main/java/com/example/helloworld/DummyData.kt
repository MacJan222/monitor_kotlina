package com.example.helloworld

import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList

class DummyData {
    var timer = Timer()
    var rnd = 0.0F
    var arr: ArrayList<Float> = arrayListOf()
    val calibrationTime = 100

    var monitor = object : TimerTask() {
        override fun run() {
            rnd=randomNumber()
            if(arr.size>calibrationTime){
                arr.removeAt(0)
            }
            arr+=rnd
            //println(arr.size)
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
                rnd=randomNumber()
                if(arr.size>calibrationTime){
                    arr.removeAt(0)
                }
                arr+=rnd
                //println(arr.size)
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