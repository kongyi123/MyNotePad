package com.example.mynotepad

import android.app.ActivityManager
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import com.example.model.DataManager
import com.example.mynotepad.activity.MainActivity
import com.example.mynotesheet.NoteSheetActivity
import com.example.paperweight.PaperWeightActivity
import com.example.personalcalendar.activity.PcalendarActivity
import com.example.sharedcalendar.activity.CalendarActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class AccessActivity : AppCompatActivity() {
    private lateinit var mPhoneNumber:String
    var isAdmin:Boolean = false
    private var isHcntReady:AtomicBoolean = AtomicBoolean(false)
    private val isDBReady:AtomicBoolean = AtomicBoolean(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_access)
        mPhoneNumber = DataManager.getLineNumber(this, this) // context 정보가 null이 아니려면 onCreate 에서 this를 넣어줘야.
        // onCreate 이전에는 null이다.
        if (mPhoneNumber == "+821027740931"
            || mPhoneNumber == "+821040052032") {
            isAdmin = true
        }

        makeDBReady()

        /*  From Google's docs on Android 8.0 behavior changes:

            The system allows apps to call Context.startForegroundService()
            even while the app is in the background.
            However, the app must call that service's startForeground()
            method within five seconds after the service is created.` */

        if (DataManager.getNotificationState(this)) {
            if (!isMyServiceRunning(MyService::class.java)) {
                val intent = Intent(applicationContext, MyService::class.java)
                intent.putExtra("command", "show")
                startForegroundService(intent) // foreground service 실행을 위해 이것만 있으면 됨. 윗줄의 startService(intent)는 필요 없음.
            }
        }


    }

    private fun makeDBReady() {
        DataManager.getNewNumberForHistory()
        DataManager.getAllScheduleData("id_list")
//        DataManager.getAllSheetData("sheet_list", this)
        //DataManager.loadNotepadData(this)

        DataManager.hcnt.observe(this, androidx.lifecycle.Observer {
            isHcntReady.set(true)
        })

        CoroutineScope(Dispatchers.Main).launch {
            for (i in 1..15) {
                if (isHcntReady.get()) {
                    Log.i("kongyi0509", "DB is ready")
                    init()
                    break
                }
                delay(1000)
                Log.i("kongyi0509", "getting data from DB....")
            }

            if (!isHcntReady.get()) {
                showDBLinkFailDialog()
                //makeDBReady()
                return@launch
            } else {
                isDBReady.set(true)
            }
            findViewById<ProgressBar>(R.id.loadingIcon).visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()

        if (!isDBReady.get()) {
            makeDBReady()
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

    private fun showDBLinkFailDialog() {
        val onClickListener = DialogInterface.OnClickListener { _,_ ->
            makeDBReady()
        }
        val dlg: AlertDialog.Builder = AlertDialog.Builder(this)
            .setTitle("DB link")
            .setMessage("\nFail!!! Network may not be connected.")
            .setPositiveButton("Retry", onClickListener)
        val ad: AlertDialog = dlg.create()
//        ad.setView(view) // 메시지
        ad.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menuSettingBtn-> startActivity(Intent(this, SettingActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    private suspend fun init() {
        if (isAdmin) {
            findViewById<Button>(R.id.alarmNotiBtn).visibility = View.VISIBLE
            findViewById<Button>(R.id.myMemoBtn).visibility = View.VISIBLE
            findViewById<Button>(R.id.shareCalendarBtn).visibility = View.VISIBLE
            findViewById<Button>(R.id.personalCalendarBtn).visibility = View.VISIBLE
            findViewById<Button>(R.id.historyManagerBtn).visibility = View.VISIBLE
            findViewById<Button>(R.id.paperWeightBtn).visibility = View.VISIBLE
            findViewById<Button>(R.id.noteSheettBtn).visibility = View.VISIBLE
        }

        findViewById<Button>(R.id.alarmNotiBtn).setOnClickListener {
            startActivity(Intent(this, AlarmMainActivity::class.java))
            DataManager.putSingleHistory(this,"access", "alarmNoti", mPhoneNumber)
        }
        findViewById<Button>(R.id.myMemoBtn).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            DataManager.putSingleHistory(this, "access", "myMemo", mPhoneNumber)
        }
        findViewById<Button>(R.id.shareCalendarBtn).setOnClickListener {
            startActivity(Intent(this, CalendarActivity::class.java))
            DataManager.putSingleHistory(this, "access", "sharedCalendar", mPhoneNumber)
        }
        findViewById<Button>(R.id.personalCalendarBtn).setOnClickListener {
            startActivity(Intent(this, PcalendarActivity::class.java))
            DataManager.putSingleHistory(this, "access", "personalCalendar", mPhoneNumber)
        }
        findViewById<Button>(R.id.historyManagerBtn).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
            DataManager.putSingleHistory(this, "access", "historyManager", mPhoneNumber)
        }
        findViewById<Button>(R.id.paperWeightBtn).setOnClickListener {
            startActivity(Intent(this, PaperWeightActivity::class.java))
            DataManager.putSingleHistory(this, "access", "historyManager", mPhoneNumber)
        }
        findViewById<Button>(R.id.noteSheettBtn).setOnClickListener {
            startActivity(Intent(this, NoteSheetActivity::class.java))
            DataManager.putSingleHistory(this, "access", "historyManager", mPhoneNumber)
        }
    }
}