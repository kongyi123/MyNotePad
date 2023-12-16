package com.example.personalcalendar.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.example.model.DataManager
import com.example.personalcalendar.R

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        findViewById<Button>(R.id.setting_view_cancel).setOnClickListener {
            finish()
        }

        var notice = intent.getStringExtra("notice")
        if (notice == null) {
            notice = ""
        }
        findViewById<EditText>(R.id.setting_view_content).setText(notice.toString())

        findViewById<Button>(R.id.setting_view_ok).setOnClickListener {
            DataManager.setNotice(findViewById<EditText>(R.id.setting_view_content).text.toString())
            finish()
        }
    }
}



// asdfjkllaskjdfklj

