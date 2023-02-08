package com.example.helloworld

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isReadPermissionGranted = false
    private var isWritePermissionGranted = false
    private var isBluetoothPermissionGranted = false
    var address: String? = null
    var isConnected = false
    private val calibrator = Calibration()

    companion object {
        var ourSensor: BluetoothDevice? = null
        var bluetoothSocket: BluetoothSocket? = null
        lateinit var bluetoothAdapter: BluetoothAdapter
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permissions ->
            isReadPermissionGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: isReadPermissionGranted
            isWritePermissionGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: isWritePermissionGranted
            isBluetoothPermissionGranted = permissions[Manifest.permission.BLUETOOTH_CONNECT] ?: isBluetoothPermissionGranted
        }

        requestPermission()

        var minMax = Pair(0.0F,0.0F)

        btnBluetooth.setOnClickListener {

            if(bluetoothAdapter.isEnabled){
                getPairedDevice()
                if(address != null) {
                    ConnectToDevice(this@MainActivity).execute()
                }
            }
            else{
                bluetoothAdapter.enable()
                getPairedDevice()
                if(address != null) {
                    ConnectToDevice(this@MainActivity).execute()
                }
            }


        }

        btnData.setOnClickListener {
            startActivity(Intent(baseContext, DataScreen::class.java))
        }

        btnSensor.setOnClickListener {
            val intent = Intent(baseContext, SensorScreen::class.java)
            intent.putExtra("minMax",minMax.toString())
            startActivity(intent)
        }

        sensorCalibrate.setOnCheckedChangeListener{
                _,isChecked-> if(isChecked) {
            calibrator.startRandomData()
        } else{
            calibrator.stopRandomData()
            minMax = calibrator.getMinMax()
            btnSensor.isEnabled = true
        }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestPermission(){
        isReadPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        isWritePermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        isBluetoothPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED

        val permissionRequest : MutableList<String> = ArrayList()

        if(!isReadPermissionGranted){

            permissionRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if(!isWritePermissionGranted){
            permissionRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if(!isBluetoothPermissionGranted){
            permissionRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if(permissionRequest.isNotEmpty()){
            permissionLauncher.launch(permissionRequest.toTypedArray())
        }
    }

    @SuppressLint("MissingPermission")
    private fun getPairedDevice() {
        val pairedDevices = bluetoothAdapter.bondedDevices


        if(pairedDevices.isNotEmpty()){
            for(device: BluetoothDevice in pairedDevices){

                if (device.name == "HC-05") {
                    ourSensor = device
                    address = device.address
                }

            }
        }
        else
        {
            Toast.makeText(this@MainActivity, "Nie znaleziono czujnika! Upewnij się, że sparowałeś urządzenie!", Toast.LENGTH_SHORT).show()
        }
        if(ourSensor == null) {
            Toast.makeText(this@MainActivity, "Nie znaleziono czujnika! Upewnij się, że sparowałeś urządzenie!", Toast.LENGTH_SHORT).show()
        }

    }

    inner class ConnectToDevice(mainActivity: MainActivity) : AsyncTask<String,Void,Void>() {

        @SuppressLint("MissingPermission")
        override fun doInBackground(vararg p0: String?): Void? {
            try {
                if (bluetoothSocket == null || !isConnected) {
                    println("halo")
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(address)
                    val id = device.getUuids()
                    bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString(id[0].toString()))
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    bluetoothSocket!!.connect()
                    if (bluetoothSocket!!.isConnected){
                        isConnected = true
                    }
                    this@MainActivity.runOnUiThread {
                        findViewById<TextView>(R.id.statusText).text =
                            "Połączenie z urządzeniem: połączono"
                        findViewById<TextView>(R.id.actionReference).text =
                            "Kliknij przycisk kalibracji."
                        findViewById<ToggleButton>(R.id.sensorCalibrate).isEnabled = true
                    }
                }
            } catch (e: IOException) {
                //connectSuccess = false
                isConnected = false
                if (bluetoothSocket != null) {
                    bluetoothSocket!!.close()
                }
                e.printStackTrace()
                this@MainActivity.runOnUiThread {
                    Toast.makeText(this@MainActivity, "Nie udało się połączyć!", Toast.LENGTH_SHORT).show()
                }
            }
            return null
        }

//        override fun onProgressUpdate(vararg values: Void?) {
//            super.onProgressUpdate(*values)
//        }
//
//        override fun onCancelled() {
//            super.onCancelled()
//        }
    }

    inner class Calibration {

        var calibrationData: ArrayList<Float> = arrayListOf()
        var rawBuffer = 9999.0F
        var rawSample = 9999.0F
        var filteredSample = 9999.0F
        val samplingFrequency = 50


        val lpfCalibration = FilteringLPF(41, 20, 5, 1200,
            10, 6)

        val handler = Handler(Looper.getMainLooper())

        private val runnable = object : Runnable {
            override fun run() {

                sensorCalibrate.isEnabled = calibrationData.size >= 41

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
            val slicedArr = calibrationData.takeLast(calibrationData.size - 41)
            return Pair(slicedArr.min(), slicedArr.max())
        }
    }

    fun readBluetoothData(): Float {

        val input = BufferedReader(InputStreamReader(bluetoothSocket!!.inputStream))
        var rawData = ""
        try {
            rawData = input.readLine()
        }
        catch (e:java.lang.Exception) {
            Toast.makeText(this@MainActivity, "Bluetooth nie działa!", Toast.LENGTH_SHORT).show()
            this@MainActivity.findViewById<ToggleButton>(R.id.sensorCalibrate).isEnabled = false
        }
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
}