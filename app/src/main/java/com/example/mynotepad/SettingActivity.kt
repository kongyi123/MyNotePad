package com.example.mynotepad

import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import com.example.model.DataManager

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        val notificationState = DataManager.getNotificationState(this)
        if (notificationState) {
            findViewById<CheckBox>(R.id.notificationCheckBox).isChecked = true
        } else {
            findViewById<LinearLayout>(R.id.updateItemLayout).visibility = View.GONE
        }
        if (DataManager.getUpdateState(this)) {
            findViewById<CheckBox>(R.id.updateCheckBox).isChecked = true
        }

        findViewById<CheckBox>(R.id.notificationCheckBox).setOnClickListener {
            if (findViewById<CheckBox>(R.id.notificationCheckBox).isChecked) {
                findViewById<LinearLayout>(R.id.updateItemLayout).visibility = View.VISIBLE
            }
            else {
                findViewById<LinearLayout>(R.id.updateItemLayout).visibility = View.GONE
            }
        }

        findViewById<Button>(R.id.okButton).setOnClickListener {
            Log.i("kongyiAAA", "Clicked")
            followUp()
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        followUp()
    }

    private fun followUp() {
        setPreference()
        removeNotification()
    }

    private fun removeNotification() {
        Log.i("kongyiAAA", "removeNotification")

        val notificationCheckState = findViewById<CheckBox>(R.id.notificationCheckBox).isChecked
        if (!notificationCheckState) {
            Log.i("kongyiAAA", "notificationCheckState == false")
            val intent = Intent(applicationContext, MyService::class.java)
            stopService(intent)

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager;
            //notificationManager.cancel(2) // cancel(알림 특정 id)
            notificationManager.cancelAll() // 이전에 있던 모든 Notification 알림 제거
        } else {
            if (isMyServiceRunning(MyService::class.java)) {
                Log.i("kongyiAAA", "notificationCheckState == true")
                val intent = Intent(applicationContext, MyService::class.java)
                intent.putExtra("command", "show")
                startForegroundService(intent) // foreground service 실행을 위해 이것만 있으면 됨. 윗줄의 startService(intent)는 필요 없음.
            }
        }
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager: ActivityManager =
            getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun setPreference() {
        val notificationCheckState = findViewById<CheckBox>(R.id.notificationCheckBox).isChecked
        DataManager.setNotificationState(this, notificationCheckState)
        var updateCheckState = findViewById<CheckBox>(R.id.updateCheckBox).isChecked
        if (findViewById<CheckBox>(R.id.updateCheckBox).visibility == View.GONE) {
            updateCheckState = false
        }
        DataManager.setUpdateState(this, updateCheckState)
    }
}