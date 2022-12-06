package com.example.helloworld

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
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
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isReadPermissionGranted = false
    private var isWritePermissionGranted = false
    private var isBluetoothPermissionGranted = false
    //val sensorUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    var address: String? = null
    var isConnected = false
    //var connectSuccess = false

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

        //Manifest.permission.blue
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permissions ->
            isReadPermissionGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: isReadPermissionGranted
            isWritePermissionGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: isWritePermissionGranted
            isBluetoothPermissionGranted = permissions[Manifest.permission.BLUETOOTH_CONNECT] ?: isBluetoothPermissionGranted
        }

        requestPermission()

        val dummyData = DummyData()
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
            dummyData.startRandomData()
        } else{
            dummyData.stopRandomData()
            //dummyData.resetData()
            minMax = dummyData.getMinMax()
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
        val pairedDevices = bluetoothAdapter!!.bondedDevices


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

//        override fun onPreExecute() {
//            super.onPreExecute()
//        }

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
                    } else {
//326451
                    }
                    this@MainActivity.runOnUiThread(java.lang.Runnable {
                        findViewById<TextView>(R.id.statusText).text="Połączenie z urządzeniem: połączono"
                        findViewById<TextView>(R.id.actionReference).text="Kliknij przycisk kalibracji."
                        findViewById<ToggleButton>(R.id.sensorCalibrate).isEnabled=true
                    })

                    //.text = "Udało się połączyć z urządzeniem."
                }
            } catch (e: IOException) {
                //connectSuccess = false
                isConnected = false
                if (bluetoothSocket != null) {
                    bluetoothSocket!!.close()
                }
                e.printStackTrace()
                this@MainActivity.runOnUiThread(java.lang.Runnable {
                    Toast.makeText(this@MainActivity, "Nie udało się połączyć!", Toast.LENGTH_SHORT)
                        .show()
                })
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

}