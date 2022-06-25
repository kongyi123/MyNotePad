package com.example.accountbook

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.accountbook.ui.theme.MyNotepadTheme
import java.text.SimpleDateFormat
import java.util.*
import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import java.io.*


class accountBookActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cr = getContentResolver();
        val cursor = cr.query(
            Uri.parse("content://sms/inbox"),
            null,
            null,
            null,
            null
        )

        val nameidx = cursor?.getColumnIndex("address")
        val dateidx = cursor?.getColumnIndex("date")
        val bodyidx = cursor?.getColumnIndex("body")

        val formatter = SimpleDateFormat("yyyy.MM.dd HH:mm")
        Log.i("kongyi1220", "총 기록 개수 : " + cursor?.count + "개\n")
        var count = 0
        var organizedContent = ""
        if (cursor != null && nameidx != null && dateidx != null && bodyidx != null) {
            while (cursor.moveToNext()) {
                val name = cursor.getString(nameidx)
                if (!("15885000" in name || "15888900" in name)) continue

                // 날짜
                val date = cursor.getLong(dateidx)
                val sdate = formatter.format(Date(date))
                organizedContent += "$sdate  "

                // 내용
                val body = cursor.getString(bodyidx)
                var start = 0
                var end = 0
                if ("출금" in body) {
                    start = body.lastIndexOf("출금")
                    end = body.indexOf("원")
                    start += 3
                    Log.i("kongyi1220", "start = $start")
                    Log.i("kongyi1220", "end = $end")
                    if (start >= end) continue
                    val extracted = body.substring(start, end).replace(" ", "")
                    organizedContent += "-$extracted  "

                } else if ("입금" in body){
                    start = body.lastIndexOf("입금")
                    end = body.indexOf("원")
                    start += 3
                    if (start >= end) continue
                    val extracted = body.substring(start, end).replace(" ", "")
                    organizedContent += "$extracted  "
                }

                if (end <= 0) continue
                if (body.indexOf("잔액") <= end) continue
                val tstring = body.substring(end+2, body.indexOf("잔액")-1)
                organizedContent += "$tstring \n"

                // 최대 100개 까지만
                if (count++ == cursor.count) {
                    break
                }
            }
            cursor.close()
        }
        Log.i("kongyi1220", "The End")


        setContent {
            MyNotepadTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Greeting(organizedContent)
                }
            }
        }
    }
}

@Composable
fun Greeting(content: String) {
    Column(
        modifier = Modifier
            .background(Color.LightGray)
            .padding(15.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Button(onClick = { save(content) }) {
            Text(text = "텍스트 파일로 저장")
        }
        Text(
            text = content
        )
    }
}

private fun save(inputData:String) {
    // make and wrtie content at file
    val file = File(Environment.getExternalStorageDirectory(), "External.txt")
    //val file = "External.txt"
    if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
        Log.i("kongyi1220", "create : File path = $file")
        try {
            val fw = FileWriter(file, false)
            fw.write(inputData)
            fw.close()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.i("kongyi1220", "create : IOException ")
        }
    } else {
        Log.d("kongyi1220", "create : External Storage is not ready")
    }


    // read content from file
    val buffer = StringBuffer()
    var data: String? = null
    val fis: FileInputStream? = null

    val path = file

    try {
        val eReader = BufferedReader(FileReader(path))
        data = eReader.readLine()
        while (data != null) {
            buffer.append(data)
            data = eReader.readLine()
        }
        Log.i("kongyi1220", "read : file content = " + buffer.toString().toString() + "\n")
        eReader.close()
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        Log.i("kongyi1220", "read : FileNotFoundException")
    } catch (e: IOException) {
        e.printStackTrace()
        Log.i("kongyi1220", "read : IOException")
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyNotepadTheme {
        Greeting("Android")
    }
}