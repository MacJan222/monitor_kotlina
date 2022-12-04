package com.example.helloworld

import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.abs

fun readBluetoothData(last_sample: Float): Float? {

    val input = BufferedReader(InputStreamReader(MainActivity.bluetoothSocket!!.getInputStream()))
    val rawData = input.readLine()

//    var data = emptyList<String>()
    var data: Float? = null
    if(rawData.isNotEmpty()){
//        data = rawData.split(" ")
        data = rawData.toFloat()
    }

    var correctData: Float? = null

    if (data != null) {
        if((data > 100.0F) && (abs(data - last_sample) < 50.0F)) {
                correctData = data
            }
    }
    return correctData
}
