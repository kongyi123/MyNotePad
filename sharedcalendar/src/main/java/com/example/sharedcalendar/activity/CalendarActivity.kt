package com.example.sharedcalendar.activity

import android.content.Context
import android.content.Intent
import android.os.Build

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.common.ContextHolder
import com.example.model.DataManager
import com.example.sharedcalendar.R
import com.example.common.data.Schedule
import com.example.mychartviewlibrary.calendar.DoingThingsListener
import com.example.mychartviewlibrary.calendar.MyCalendarView
import com.example.mychartviewlibrary.calendar.OnAddBtnClickListener
import com.example.mychartviewlibrary.calendar.data.CalendarFilter
import com.example.mychartviewlibrary.calendar.list.ITask
import com.example.mychartviewlibrary.calendar.list.OnScheduleItemClickListener

class CalendarActivity : AppCompatActivity() {
    private lateinit var mContext: Context
    private var mPhoneNumber: String = ""
    private lateinit var mCalendarView: MyCalendarView

    val iTask = object : ITask {
        override fun doTaskOnSwiped(viewHolder: RecyclerView.ViewHolder) {
            val date = viewHolder.itemView.findViewById<TextView>(com.example.mychartviewlibrary.R.id.item_date).text.toString()
            val id = viewHolder.itemView.findViewById<TextView>(com.example.mychartviewlibrary.R.id.item_id).text.toString()
            val title = viewHolder.itemView.findViewById<TextView>(com.example.mychartviewlibrary.R.id.item_title).text.toString()
            val content = viewHolder.itemView.findViewById<TextView>(com.example.mychartviewlibrary.R.id.item_content).text.toString()
            val color = viewHolder.itemView.findViewById<TextView>(com.example.mychartviewlibrary.R.id.item_color).text.toString()
            DataManager.removeSingleSchedule("id_list", date, id)
            DataManager.putSingleHistory(mContext, "cal-schedule-remove", "content: id = ${id}, date = ${date}, title=$title, content=$content, color=$color}", mPhoneNumber)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)
        mContext = this
        //mPhoneNumber = DataManager.getLineNumber(this, this)
        mPhoneNumber = "+821040052032"
        ContextHolder.setPhoneNumber(mPhoneNumber)
        mCalendarView = findViewById<MyCalendarView>(R.id.myCalendarView)

        mCalendarView.setDateRange(2021, 2024) // if the end of year changed to 2025, then loading is not finish. doesn't know the reason for now.
        mCalendarView.initializeCalendar(iTask)

        DataManager.getLastFilterSettingState(this)?.let { calendarFilterFromDB ->
            val calendarFilter = CalendarFilter(
                calendarFilterFromDB.colorFilter,
                calendarFilterFromDB.keyword,
                calendarFilterFromDB.mode
            )
            mCalendarView.loadFilterInfo(calendarFilter)
            Log.i("kongyi0516", "loadComplete")
        }

        Log.i("kongyi0521", "hi")
        DataManager.dataList.observe(this, androidx.lifecycle.Observer { scheduleList ->
            Log.i("kongyi0516", "dataList.observe")

            val scheduleItemClickListener = object : OnScheduleItemClickListener {
                override fun onItemClick(schedule: Schedule) {
                    val intent = Intent(this@CalendarActivity, DayActivity::class.java)
                    Log.i("kongyi0505", "schedule = $schedule");
                    intent.putExtra("info", schedule);
                    startActivity(intent);
                }
            }
            val doingThings = object : DoingThingsListener {
                override fun runTask() {
                    Log.i("kongyi0515-2", "runTask() is called")
                    val mFilter = com.example.model.data.CalendarFilter(
                        mCalendarView.mColorFilter,
                        mCalendarView.mKeyword,
                        mCalendarView.mSelectedMode)
                    DataManager.setUpdateFilterSettingState(this@CalendarActivity, mFilter)
                }
            }
            mCalendarView.setOnItemClickListener(scheduleList, scheduleItemClickListener)
            mCalendarView.setSchedules(scheduleList)
            mCalendarView.refresh(scheduleList, scheduleItemClickListener, doingThings) // it is needed
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
            Log.i("kongyi0516", "observe complete time = ${System.currentTimeMillis()}")
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