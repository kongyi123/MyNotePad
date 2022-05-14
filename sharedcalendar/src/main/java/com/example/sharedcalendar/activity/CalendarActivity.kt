package com.example.sharedcalendar.activity

import android.content.Context
import android.content.Intent
import android.os.Build

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.common.ContextHolder
import com.example.model.DataManager
import com.example.sharedcalendar.R
import com.example.common.data.Schedule
import com.example.mychartviewlibrary.calendar.MyCalendarView
import com.example.mychartviewlibrary.calendar.OnAddBtnClickListener
import com.example.mychartviewlibrary.calendar.list.OnScheduleItemClickListener


class CalendarActivity : AppCompatActivity() {

    private lateinit var mContext: Context
    private lateinit var mPhoneNumber: String
    private lateinit var mCalendarView: MyCalendarView

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)
        mContext = this
        DataManager.getAllScheduleData("id_list")

        mPhoneNumber = DataManager.getLineNumber(this, this)
        ContextHolder.setPhoneNumber(mPhoneNumber)

        mCalendarView = findViewById<MyCalendarView>(R.id.myCalendarView)
        DataManager.dataList.observe(this, androidx.lifecycle.Observer { scheduleList ->
            Log.i("kongyi0508", "dataList.observe")

            val scheduleItemClickListener = object : OnScheduleItemClickListener {
                override fun onItemClick(schedule: Schedule) {
                    val intent = Intent(this@CalendarActivity, DayActivity::class.java)
                    Log.i("kongyi0505", "schedule = $schedule");
                    intent.putExtra("info", schedule);
                    startActivity(intent);
                }
            }
            mCalendarView.setOnItemClickListener(scheduleList, scheduleItemClickListener)
            mCalendarView.setSchedules(scheduleList)
            mCalendarView.refresh(scheduleList, scheduleItemClickListener) // it is needed
            mCalendarView.mCurrentDate?.let {
                mCalendarView.loadDataAtList(
                    scheduleList,
                    mCalendarView.mCurrentDate!!,
                    scheduleItemClickListener
                )
            }
            val addBtnListener = object : OnAddBtnClickListener {
                override fun onItemClick(date: String) {
                    Log.i("kongyi0506", "date = $date")
                    val intent = Intent(this@CalendarActivity, DayActivity::class.java)
                    val schedule = Schedule("no_id", date, "", "", "")
                    intent.putExtra("info", schedule)
                    startActivity(intent);
                }
            }
            mCalendarView.setAddScheduleBtn(addBtnListener)
            Log.i("kongyi0507", "observe complete time = ${System.currentTimeMillis()}")
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_item_search) {
            if (mCalendarView.isShownFilter) {
                mCalendarView.setFilterVisibility(false)
            } else {
                mCalendarView.setFilterVisibility(true)
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_option, menu)
        return true
    }
}