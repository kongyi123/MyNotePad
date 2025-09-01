package com.example.mynotepad

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.common.R
import com.example.model.DataManager
import com.example.model.data.History

class HistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val historyView = findViewById<TextView>(R.id.historyView)
        var mHistoryList = ArrayList<History>()
        //DataManager.getAllHistoryData()
        DataManager.hList.observe(this, androidx.lifecycle.Observer {
            mHistoryList = it
            var str = ""
            var cnt = 100
            for (history: History in mHistoryList.reversed() ) {
                if (cnt <= 0) break
                cnt --;
                str += history.toString() + "\n\n"
            }

            historyView.text = str
        })
    }
}