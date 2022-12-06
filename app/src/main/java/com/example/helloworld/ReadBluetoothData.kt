package com.example.helloworld

import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.abs

fun readBluetoothData(): Float {

    val input = BufferedReader(InputStreamReader(MainActivity.bluetoothSocket!!.getInputStream()))
    val rawData = input.readLine()

    var data = 9999.0F
    var correctData = 9999.0F

    if(rawData.isNotEmpty()){
        data = rawData.toFloat()
    }

    if(data > 100.0F) {
        correctData = data
    }
    return correctData
}
