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
import android.view.View
import android.view.View.OnClickListener
import android.widget.*
import androidx.annotation.RequiresApi
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
    private val TAG = "MyCalendarView"
    private var calendarMaxPage = 0
    private var startYear = 0
    private var endYear = 0

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
    private var mListener: OnScheduleItemClickListener? = null
    private var mDoingThings: DoingThingsListener? = null

    var mColorFilter = ArrayList<String>()
    var mKeyword = ArrayList<String>() // default
    var mSelectedMode = ArrayList<Int>()

    init {
        ContextHolder.setContext(this.context)
        inflate(context, R.layout.my_calendar, this)
        mTitlePager = findViewById(R.id.calendar_title)
        mCalendarPager = findViewById(R.id.calendar_vpPager)
        mRecyclerView = findViewById(R.id.calendar_recyclerView)
    }

    fun setDateRange(startYear: Int, endYear: Int) {
        this.startYear = startYear
        this.endYear = endYear
        this.calendarMaxPage = (endYear-startYear+1)*12
    }

    fun loadFilterInfo(calendarFilter: CalendarFilter) {
        Log.i("kongyi0515-5", "loadFilterInfo")
        this.mColorFilter = calendarFilter.colorFilter
        this.mKeyword = calendarFilter.keyword
        this.mSelectedMode = calendarFilter.mode

        Log.i("kongyi0515-5", "mColorFilter = $mColorFilter")
        Log.i("kongyi0515-5", "mKeyword = $mKeyword")
        Log.i("kongyi0515-5", "mSelectedMode = $mSelectedMode")

        findViewById<EditText>(R.id.edit_keyword).setText(mKeyword[0])
        findViewById<CheckBox>(R.id.check_red).isChecked = false
        findViewById<CheckBox>(R.id.check_orange).isChecked = false
        findViewById<CheckBox>(R.id.check_yellow).isChecked = false
        findViewById<CheckBox>(R.id.check_green).isChecked = false
        findViewById<CheckBox>(R.id.check_blue).isChecked = false
        findViewById<CheckBox>(R.id.check_purple).isChecked = false
        for (color in mColorFilter) {
            if (color == "red") findViewById<CheckBox>(R.id.check_red).isChecked = true
            if (color == "orange") findViewById<CheckBox>(R.id.check_orange).isChecked = true
            if (color == "yellow") findViewById<CheckBox>(R.id.check_yellow).isChecked = true
            if (color == "green") findViewById<CheckBox>(R.id.check_green).isChecked = true
            if (color == "blue") findViewById<CheckBox>(R.id.check_blue).isChecked = true
            if (color == "purple") findViewById<CheckBox>(R.id.check_purple).isChecked = true
        }
        if (mSelectedMode[0] == 0) {
            findViewById<RadioButton>(R.id.or_radio_button).isChecked = true
            findViewById<RadioButton>(R.id.and_radio_button).isChecked = false
        } else {
            findViewById<RadioButton>(R.id.or_radio_button).isChecked = false
            findViewById<RadioButton>(R.id.and_radio_button).isChecked = true
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
    fun refresh(scheduleList: ArrayList<Schedule>, scheduleItemClickListener: OnScheduleItemClickListener?, doingThings:DoingThingsListener?) {
        Log.i("kongyi0515-5", "refresh()")

        mKeyword.clear()
        mKeyword.add(findViewById<EditText>(R.id.edit_keyword).text.toString())
        mSelectedMode.clear()
        if (findViewById<RadioGroup>(R.id.mode_radio_group).checkedRadioButtonId == R.id.or_radio_button) {
            mSelectedMode.add(0)
        } else {
            mSelectedMode.add(1)
        }
        updateColorFilter()
        if (doingThings != null) {
            mDoingThings = doingThings
            doingThings.runTask() // save filter information.
        }

        mCalendarPager.adapter?.notifyDataSetChanged()
        val mFilter = ArrayList<CalendarFilter>()
        mFilter.add(CalendarFilter(mColorFilter, mKeyword, mSelectedMode))
        (mCalendarPager.adapter as RecyclerViewAdapterForCalendar).setFilter(mFilter)
        Log.i("kongyi0515-5", "refresh : keyword = $mKeyword")
        Log.i("kongyi0515-5", "refresh : mSelectedMode = $mSelectedMode")
        Log.i("kongyi0515-5", "refresh : mColorFilter = $mColorFilter")


        if (mCurrentDate != null) {
            Log.i("kongyi0515-3", "mCurrentDate = $mCurrentDate")
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

    fun initializeCalendar() {
        Log.i("kongyi0515-5", "initializeCalendar")
        mTodayPosition = 0
        var weight = 1
        val cal = Calendar.getInstance()
        cal.timeInMillis = System.currentTimeMillis()
        for (year in startYear..endYear) {
            for (month in 1..12) {
                mTodayPosition += weight
                if (cal.get(Calendar.YEAR) == year &&
                    cal.get(Calendar.MONTH) == month
                ) {
                    weight = 0
                }
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
        Log.i("kongyi0515-5", "mColorFilter = $mColorFilter, mKeyword = $mKeyword, mSelectedMode = $mSelectedMode")
        val mFilter = ArrayList<CalendarFilter>()
        mFilter.add(CalendarFilter(mColorFilter, mKeyword, mSelectedMode))
        mCalendarAdapter = RecyclerViewAdapterForCalendar(context, mMap, mFilter, startYear, endYear)
        mCalendarPager.adapter = mCalendarAdapter
        mCalendarPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        mCalendarPager.registerOnPageChangeCallback(onPageChangeCallbackForCalendar)
        Log.i("kongyi0505", "mTodayPosition = ${mTodayPosition}")
        mCalendarPager.setCurrentItem(mTodayPosition, false)


        mTitlePager.adapter = RecyclerViewAdapterForTitle(startYear, endYear)
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
            if (current < calendarMaxPage) {
                mCalendarPager.setCurrentItem(current + 1, true)
            }
        }
        findViewById<CheckBox>(R.id.check_red).setOnClickListener { refresh(mScheduleList, mListener, mDoingThings) }
        findViewById<CheckBox>(R.id.check_orange).setOnClickListener { refresh(mScheduleList, mListener, mDoingThings) }
        findViewById<CheckBox>(R.id.check_yellow).setOnClickListener { refresh(mScheduleList, mListener, mDoingThings) }
        findViewById<CheckBox>(R.id.check_green).setOnClickListener { refresh(mScheduleList, mListener, mDoingThings) }
        findViewById<CheckBox>(R.id.check_blue).setOnClickListener { refresh(mScheduleList, mListener, mDoingThings) }
        findViewById<CheckBox>(R.id.check_purple).setOnClickListener { refresh(mScheduleList, mListener, mDoingThings) }

        findViewById<RadioGroup>(R.id.mode_radio_group).setOnCheckedChangeListener { radioGroup, i ->
            refresh(mScheduleList, mListener, mDoingThings)
        }

        findViewById<EditText>(R.id.edit_keyword).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                if (mDoingThings != null) {
                    refresh(mScheduleList, mListener, mDoingThings)
                }
            }
        })

        findViewById<Button>(R.id.calendar_deleteAllBtn).setOnClickListener {
            mCurrentDate?.let {
                showDialog(it)
            }
        }
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

    fun loadDataAtList(mScheduleList: ArrayList<Schedule>?, selectedDate:String, listener:OnScheduleItemClickListener?) {
        Log.i(TAG, "loadDataAtList()")
        val manager = LinearLayoutManager(ContextHolder.getContext(), LinearLayoutManager.VERTICAL, false)
        mRecyclerView.layoutManager = manager
        val mFilter = ArrayList<CalendarFilter>()
        mFilter.add(CalendarFilter(mColorFilter, mKeyword, mSelectedMode))
        Log.i("kongyi0515-5", "mFilter = ${mFilter[0]}")
        listener?.let {
            mRecyclerView.adapter = DayListAdapter(mScheduleList!!, selectedDate, listener, mFilter, mCalendarAdapter)
            (mRecyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(0, 0)
        }
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
