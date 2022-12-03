package com.example.helloworld

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.Debug.getState
import com.androidplot.xy.*
import kotlinx.android.synthetic.main.data_screen.*
import java.io.File
import java.io.InputStream
import java.nio.charset.Charset
import java.text.FieldPosition
import java.text.Format
import java.text.ParsePosition


class DataScreen : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.data_screen)

        // TO JEST ZOOM I PAN aka przesuwanie i przyblizanie wykresu
        PanZoom.attach(dataPlot)

        btnDataPlot.setOnClickListener{
            dataPlot.clear()
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
            val seriesFormat = LineAndPointFormatter(Color.BLUE, Color.BLACK,null,null)
            val series: XYSeries = SimpleXYSeries(values, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "")
            dataPlot.addSeries(series,seriesFormat)
            dataPlot.redraw()

            dataPlot.graph.getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).format = object : Format() {
                override fun format(
                    obj: Any?,
                    toAppendTo: StringBuffer,
                    pos: FieldPosition
                ) : StringBuffer {
                    val i = Math.round((obj as Number).toFloat())
                    return toAppendTo.append(values[i])
                }

                override fun parseObject(p0: String?, p1: ParsePosition?): Any? {
                    return null
                }


            }

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