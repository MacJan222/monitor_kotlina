package com.example.helloworld

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import kotlinx.android.synthetic.main.data_screen.*
import kotlinx.android.synthetic.main.sensor_screen.*
import java.io.File
import java.io.InputStream
import java.nio.charset.Charset


class DataScreen : Activity() {

    lateinit var lineList: java.util.ArrayList<Entry>
    lateinit var lineDataSet: LineDataSet
    lateinit var lineData: LineData
    lateinit var peakList: java.util.ArrayList<Entry>
    lateinit var peakDataSet: ScatterDataSet
    lateinit var peakData: ScatterData
    var combinedData = CombinedData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.data_screen)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        lineList = java.util.ArrayList()
        lineDataSet = LineDataSet(lineList, "Dane z czujnika")
        lineDataSet.setDrawCircles(false)
        lineDataSet.setDrawValues(false)
        lineDataSet.setDrawHighlightIndicators(false)
        lineDataSet.color = Color.RED
        lineData = LineData(lineDataSet)

        peakList = java.util.ArrayList()
        peakDataSet = ScatterDataSet(peakList, "Wykryte peaki")
        peakDataSet.setDrawValues(false)
        peakDataSet.setDrawHighlightIndicators(false)
        peakDataSet.color = Color.GREEN
        peakData = ScatterData(peakDataSet)


        ccDataPlot.setPinchZoom(true)

        ccDataPlot.axisLeft.setDrawGridLines(false)
        ccDataPlot.axisRight.isEnabled = false
        ccDataPlot.axisRight.setDrawGridLines(false)
        ccDataPlot.description.isEnabled = false
        ccDataPlot.axisLeft.textColor = Color.WHITE
//        ccDataPlot.setBackgroundColor(Color.WHITE)
//        ccDataPlot.setScaleEnabled(false)



        btnDataPlot.setOnClickListener{
            val intent = Intent()
            intent.setDataAndType(Uri.parse(Environment.getExternalStorageDirectory().path), "*/*")
            intent.action = Intent.ACTION_GET_CONTENT

            startActivityForResult(Intent.createChooser(intent, "Select a file"), 777)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 777 && resultCode == AppCompatActivity.RESULT_OK) {

            val selectedFile = data?.data?.path //The uri with the location of the file
            val fileName = selectedFile?.split("/")?.last()
            val path = baseContext.getExternalFilesDir(null).toString().removeSuffix("files")
            val selectedFilePath = path + fileName

            val values = getTextContent(selectedFilePath).split("\r?\n|\r".toRegex()).dropLast(1)

            lineDataSet.clear()
            peakDataSet.clear()

            var i = 0
            var numberOfBreaths = 0
            for (value in values) {
                println(value)
                val splitValues = value.split(',')
                lineDataSet.addEntry(Entry(i.toFloat(), splitValues[0].toFloat()))
                if(splitValues[1].toBoolean()) {
                    peakDataSet.addEntry(Entry(i.toFloat(), splitValues[0].toFloat()))
                    numberOfBreaths += 1
                }
                i += 1
            }
            ccDataPlot.xAxis.axisMaximum = i.toFloat()
            ccDataPlot.xAxis.axisMinimum = 0.0F
            ccDataPlot.setVisibleYRangeMaximum(140.0F, YAxis.AxisDependency.LEFT)
            ccDataPlot.moveViewTo(0.0F, 50.0F, YAxis.AxisDependency.LEFT)
            ccSensorPlot.axisLeft.axisMinimum = 0.0F
            ccSensorPlot.axisLeft.axisMaximum = 100F
            combinedData.setData(lineData)
            combinedData.setData(peakData)
            ccDataPlot.data = combinedData
            combinedData.notifyDataChanged()
            ccDataPlot.invalidate()
//            ccDataPlot.fitScreen()
        }
    }


    private fun getTextContent(pathFilename: String): String {

        val fileobj = File( pathFilename )

        if (!fileobj.exists()) {

            println("Path does not exist")

        } else {

            println("Path to read exist")
        }

        println("Path to the file:")
        println(pathFilename)

        return if (fileobj.exists() && fileobj.canRead()) {

            val ins: InputStream = fileobj.inputStream()

            ins.readBytes().toString(Charset.defaultCharset())

        }else{

            "Some error, Not found the File, or app has not permissions: $pathFilename"
        }
    }

}