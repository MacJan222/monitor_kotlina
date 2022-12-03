package com.example.helloworld

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.Debug.getState
import com.androidplot.xy.*
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.android.synthetic.main.data_screen.*
import java.io.File
import java.io.InputStream
import java.nio.charset.Charset
import java.text.FieldPosition
import java.text.Format
import java.text.ParsePosition


class DataScreen : Activity() {

    lateinit var lineList: ArrayList<Entry>
    lateinit var lineDataSet: LineDataSet
    lateinit var lineData: LineData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.data_screen)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        lineList = ArrayList()
        lineList.add(Entry(10f, 100f))
        lineList.add(Entry(20f, 200f))
        lineList.add(Entry(30f, 400f))
        lineList.add(Entry(40f, 600f))
        lineList.add(Entry(50f, 200f))
        lineList.add(Entry(60f, 300f))
        lineList.add(Entry(70f, 500f))

        lineDataSet = LineDataSet(lineList, "Count")
        lineData = LineData(lineDataSet)
        lcChart.data = lineData
        lineDataSet.setColors(*ColorTemplate.JOYFUL_COLORS)
        lineDataSet.valueTextColor = Color.BLUE
        lineDataSet.valueTextSize = 20f

        btnDataPlot.setOnClickListener{
            //PanZoom.attach(dataPlot).zoom = null
            //var dataPlotPanZoom = PanZoom.attach(dataPlot)
            val intent = Intent()
            //val z =Uri.parse(baseContext.getExternalFilesDir(null)?.getPath()
               //     +  File.separator + "myFolder" + File.separator)
            //println(z)
            intent.setDataAndType(Uri.parse(Environment.getExternalStorageDirectory().getPath()), "*/*")
            intent.setAction(Intent.ACTION_GET_CONTENT)

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
            //pathToFile.text = getTextContent(path+fileName)
            //var values: ArrayList<Float> = arrayListOf()
            val values = getTextContent(selectedFilePath).split("\r?\n|\r".toRegex()).dropLast(1).map {it.toFloat()}
            //println("AAA"+values)


        }
    }


    fun getTextContent(pathFilename: String): String {

        val fileobj = File( pathFilename )

        if (!fileobj.exists()) {

            println("Path does not exist")

        } else {

            println("Path to read exist")
        }

        println("Path to the file:")
        println(pathFilename)

        if (fileobj.exists() && fileobj.canRead()) {

            var ins: InputStream = fileobj.inputStream()
            var content = ins.readBytes().toString(Charset.defaultCharset())
            return content

        }else{

            return "Some error, Not found the File, or app has not permissions: " + pathFilename
        }
    }

}