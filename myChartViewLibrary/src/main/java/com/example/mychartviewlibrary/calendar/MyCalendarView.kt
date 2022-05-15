package com.example.mychartviewlibrary.calendar

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.util.SparseArray
import android.view.KeyEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnKeyListener
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.common.ContextHolder
import com.example.common.data.Schedule
import com.example.model.DataManager
import com.example.mychartviewlibrary.R
import com.example.mychartviewlibrary.calendar.data.CalendarFilter
import com.example.mychartviewlibrary.calendar.data.DateItem
import com.example.mychartviewlibrary.calendar.list.DayListAdapter
import com.example.mychartviewlibrary.calendar.list.OnScheduleItemClickListener
import java.util.*


// 뷰를 두번 열면 점의 개수가 두배가 됨.
// 오늘 날짜가 아니라 하루 전 날짜가 오늘 날짜로 보임

class MyCalendarView : FrameLayout {
    val TAG = "MyCalendarView"

    constructor(context: Context) : super(context){
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
    }

    private var mRecyclerView: RecyclerView
    private val mMap = SparseArray<ArrayList<Schedule>>()

    private lateinit var mCalendarAdapter: RecyclerViewAdapterForCalendar
    var mCurrentDate:String? = null
    var mTitlePager: ViewPager2
    private var mCalendarPager: ViewPager2
    var mCurrentPosition:Int? = null
    var mTodayPosition = 0

    var isShownFilter = false
    private var mScheduleList = ArrayList<Schedule>()
    private lateinit var mListener: OnScheduleItemClickListener

    private val mColorFilter = ArrayList<String>()
    private var mKeyword = ArrayList<String>() // default
    private val mSelectedMode = ArrayList<Int>()

    init {
        ContextHolder.setContext(this.context)
        inflate(context, R.layout.my_calendar, this)
        mTitlePager = findViewById(R.id.calendar_title)
        mCalendarPager = findViewById(R.id.calendar_vpPager)
        mRecyclerView = findViewById(R.id.calendar_recyclerView)
        initializeCalendar()
        findViewById<Button>(R.id.calendar_deleteAllBtn).setOnClickListener {
            mCurrentDate?.let {
                showDialog(it)
            }
        }
    }

    fun setAddScheduleBtn(listener: OnAddBtnClickListener) {
        findViewById<Button>(R.id.calendar_addScheduleBtn).setOnClickListener(OnClickListener {
            mCurrentDate?.let {
                listener.onItemClick(mCurrentDate!!)
            }
        })
    }

    private fun showDialog(date:String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle("※ 경고 ※")
        builder.setMessage("정말로 다 지우겠습니까?")
        builder.setPositiveButton("예") { dialog, which ->
            DataManager.removeDayAllSchedule("id_list", date)
            initializeCalendar()
        }
        builder.setNegativeButton("아니오") { dialog, which ->}
        builder.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refresh(scheduleList: ArrayList<Schedule>, scheduleItemClickListener: OnScheduleItemClickListener) {
        Log.i(TAG, "refresh()")
        mKeyword.clear()
        mKeyword.add(findViewById<EditText>(R.id.edit_keyword).text.toString())
        mSelectedMode.clear()
        if (findViewById<RadioGroup>(R.id.mode_radio_group).checkedRadioButtonId == R.id.or_radio_button) {
            mSelectedMode.add(0)
        } else {
            mSelectedMode.add(1)
        }
        Log.i("kongyi0515", "keyword = $mKeyword")
        updateColorFilter()
        mCalendarPager.adapter?.notifyDataSetChanged()
        if (mCurrentDate != null) {
            loadDataAtList(scheduleList, mCurrentDate!!, scheduleItemClickListener)
        }
    }

    private fun updateColorFilter() {
        mColorFilter.clear()
        if (findViewById<CheckBox>(R.id.check_red).isChecked) mColorFilter.add("red")
        if (findViewById<CheckBox>(R.id.check_orange).isChecked) mColorFilter.add("orange")
        if (findViewById<CheckBox>(R.id.check_yellow).isChecked) mColorFilter.add("yellow")
        if (findViewById<CheckBox>(R.id.check_green).isChecked) mColorFilter.add("green")
        if (findViewById<CheckBox>(R.id.check_blue).isChecked) mColorFilter.add("blue")
        if (findViewById<CheckBox>(R.id.check_purple).isChecked) mColorFilter.add("purple")
    }

    private fun initializeCalendar() {
        val calendarData: ArrayList<ArrayList<DateItem>> = ArrayList()
        val titleData: ArrayList<String> = ArrayList()
        mTodayPosition = 0
        var weight = 1
        val cal = Calendar.getInstance()
        cal.timeInMillis = System.currentTimeMillis()
        for (year in 2021..2023) {
            for (month in 1..12) {
                mTodayPosition += weight
                if (cal.get(Calendar.YEAR) == year &&
                    cal.get(Calendar.MONTH) == month
                ) {
                    weight = 0
                }
                val monthForCalendarLib = month - 1
                calendarData.add(getMonthData(year, monthForCalendarLib))
                titleData.add("${month}월 $year")
            }
        }
        if (mCurrentPosition != null) {
            mTodayPosition = mCurrentPosition!!
        }
        // 여기까지 달력 데코가 아닌 순수히 달력 초기 뷰만 셋팅하는 부분

        val onPageChangeCallbackForCalendar = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                mCurrentPosition = position
                mTitlePager.setCurrentItem(position, true)
            }
        }

        if (isShownFilter) updateColorFilter()
        Log.i("kongyi0515", "mColorFileter = $mColorFilter, mKeyword = $mKeyword")
        val mFilter = CalendarFilter(mColorFilter, mKeyword, mSelectedMode)
        mCalendarAdapter = RecyclerViewAdapterForCalendar(context, calendarData, mMap, mFilter)
        mCalendarPager.adapter = mCalendarAdapter
        mCalendarPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        mCalendarPager.registerOnPageChangeCallback(onPageChangeCallbackForCalendar)
        Log.i("kongyi0505", "mTodayPosition = ${mTodayPosition}")
        mCalendarPager.setCurrentItem(mTodayPosition, false)

        mTitlePager.adapter = RecyclerViewAdapterForTitle(titleData)
        mTitlePager.orientation = ViewPager2.ORIENTATION_VERTICAL
        mTitlePager.setCurrentItem(mTodayPosition, false)

        findViewById<TextView>(R.id.calendar_previous).setOnClickListener {
            val current = mCalendarPager.currentItem
            if (current > 0) {
                mCalendarPager.setCurrentItem(current - 1, true)
            }
        }
        findViewById<TextView>(R.id.calendar_next).setOnClickListener {
            val current = mCalendarPager.currentItem
            if (current < calendarData.size) {
                mCalendarPager.setCurrentItem(current + 1, true)
            }
        }
        findViewById<CheckBox>(R.id.check_red).setOnClickListener { refresh(mScheduleList, mListener) }
        findViewById<CheckBox>(R.id.check_orange).setOnClickListener { refresh(mScheduleList, mListener) }
        findViewById<CheckBox>(R.id.check_yellow).setOnClickListener { refresh(mScheduleList, mListener) }
        findViewById<CheckBox>(R.id.check_green).setOnClickListener { refresh(mScheduleList, mListener) }
        findViewById<CheckBox>(R.id.check_blue).setOnClickListener { refresh(mScheduleList, mListener) }
        findViewById<CheckBox>(R.id.check_purple).setOnClickListener { refresh(mScheduleList, mListener) }

        findViewById<RadioGroup>(R.id.mode_radio_group).setOnCheckedChangeListener { radioGroup, i ->
            refresh(mScheduleList, mListener)
        }



        val watcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                refresh(mScheduleList, mListener)
            }
        }
        findViewById<EditText>(R.id.edit_keyword).addTextChangedListener(watcher)


        findViewById<EditText>(R.id.edit_keyword).setOnKeyListener(object : OnKeyListener {
            override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                    Log.i("test", "keycode delete")
                }
                return false
            }

        })


    }



    fun setOnItemClickListener(mScheduleList: ArrayList<Schedule>?, listener: OnScheduleItemClickListener) {
        mListener = listener
        mCalendarAdapter.listener = object : OnDateItemClickListener {
            override fun onItemClick(
                holder: RecyclerViewAdapterForCalendar.ViewHolder,
                view: View,
                position: Int,
                dateItem: DateItem
            ) {
                Log.i(TAG, "setOnItemClickListener")
                // val schedule = Schedule("no_id","${date.year}~${Utils.addFront0(date.month.toString())}~${Utils.addFront0(date.day.toString())}", "", "", "")

                val selectedDate = "${dateItem.year}~${Utils.addFront0(dateItem.month.toString())}~${Utils.addFront0(dateItem.date.toString())}"
                mCurrentDate = selectedDate
                loadDataAtList(mScheduleList, selectedDate, listener)
            }
        }
    }

    fun loadDataAtList(mScheduleList: ArrayList<Schedule>?, selectedDate:String, listener:OnScheduleItemClickListener) {
        Log.i(TAG, "loadDataAtList()")
        val manager = LinearLayoutManager(ContextHolder.getContext(), LinearLayoutManager.VERTICAL, false)
        mRecyclerView.layoutManager = manager
        val mFilter = CalendarFilter(mColorFilter, mKeyword, mSelectedMode)
        mRecyclerView.adapter = DayListAdapter(mScheduleList!!, selectedDate, listener, mFilter)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun setSchedules(mScheduleList: ArrayList<Schedule>?) {
        Log.i(TAG, "setSchedules")
        mMap.clear()
        if (mScheduleList != null) {
            this.mScheduleList = mScheduleList
            for (schedule in mScheduleList) {
                Log.i(TAG, "schedule.title = ${schedule.title}")

                val day = Utils.getMyDateFromStringToDateItem(schedule.date)
                if (day != null) {
                    if (mMap[day.getKey()] == null) {
                        val list = ArrayList<Schedule>(1)
                        list.add(schedule)
                        mMap.append(day.getKey(), list)
                    } else {
                        val list = mMap[day.getKey()]
                        list.add(schedule)
                        mMap.append(day.getKey(), list)
                    }
                }
            }
        }
    }

    override fun onViewRemoved(child: View?) {
        super.onViewRemoved(child)
        ContextHolder.setContext(null)
    }
    // 뷰를 넣어야함...
    private fun getMonthData(year:Int, month:Int): ArrayList<DateItem> {
        val cal = Calendar.getInstance()
        cal.set(year, month, 1)
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val dayOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val dates = arrayListOf(
            DateItem(text = "일"), DateItem(text = "월"), DateItem(text = "화"), DateItem(text = "수"),
            DateItem(text = "목"), DateItem(text = "금"), DateItem(text = "토"))

        for (i in 1 until dayOfWeek) {
            dates.add(DateItem(text = ""))
        }
        var cnt = 0
        for (i in 1..dayOfMonth) {
            cnt++
            dates.add(DateItem(year, month, cnt, 1, cnt.toString()))
        }
        for (i in 0..30) {
            dates.add(DateItem(text = ""))
        }

        return dates
    }

    fun setFilterVisibility(flag:Boolean) {
        if (flag) {
            findViewById<LinearLayout>(R.id.filterLayout).visibility = View.VISIBLE
            isShownFilter = true
        } else {
            findViewById<LinearLayout>(R.id.filterLayout).visibility = View.GONE
            isShownFilter = false
        }
    }
}
