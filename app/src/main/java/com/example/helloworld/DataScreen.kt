package com.example.helloworld

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.data_screen.*
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.nio.charset.Charset

class DataScreen : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.data_screen)

        btnDataPlot.setOnClickListener{
            val intent = Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT)

            startActivityForResult(Intent.createChooser(intent, "Select a file"), 777)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 777 && resultCode == AppCompatActivity.RESULT_OK) {

            val selectedFile = data?.data //The uri with the location of the file

            // Get the File path from the Uri

            println(selectedFile?.lastPathSegment)
            val path = selectedFile?.lastPathSegment.toString().removePrefix("raw:")
            println(path)
            pathToFile.text = getTextContent(path)

            val bufferedReader: BufferedReader = File(selectedFile.toString()).bufferedReader()
            val inputString = bufferedReader.use { it.readText() }
            println(inputString)
            //val lines = this::class.java.getResourceAsStream(selectedFile.toString())?.bufferedReader()?.readLines()
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