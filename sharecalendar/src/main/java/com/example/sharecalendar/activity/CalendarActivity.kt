package com.example.sharecalendar.activity

import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.CalendarMode
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import java.util.*
import android.text.style.ForegroundColorSpan

import com.prolificinteractive.materialcalendarview.DayViewFacade

import com.prolificinteractive.materialcalendarview.DayViewDecorator
import android.text.style.RelativeSizeSpan

import android.text.style.StyleSpan

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.*

import android.os.Bundle
import android.text.style.LineBackgroundSpan
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharecalendar.DataManager
import com.example.sharecalendar.R
import com.example.sharecalendar.Utils
import com.example.sharecalendar.data.Schedule
import com.example.sharecalendar.list.DayListAdapter
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import kotlin.collections.HashMap


class CalendarActivity : AppCompatActivity() {

    private var mScheduleList: ArrayList<Schedule>? = null
    private var rvAdapter:DayListAdapter? = null
    private lateinit var mRecyclerView:RecyclerView
    private lateinit var mContext: Context
    private var mCurrentDate:CalendarDay? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)
        val calView = findViewById<MaterialCalendarView>(R.id.calendarView)
        mRecyclerView = findViewById(R.id.recyclerView)

        mContext = this
        DataManager.getAllScheduleData()
        initializeCalendar(calView)
        initializeDayListView(calView)
    }

    private fun initializeCalendar(calView: MaterialCalendarView) {
        calView.setDateTextAppearance(1); // 날짜 선택했을 때, 날짜 text가 색깔이 검은 색이 되도록 함. 이거 안쓰면 흰색됨.

        calView.state().edit()
            .setFirstDayOfWeek(Calendar.SUNDAY)
            .setMinimumDate(CalendarDay.from(2017, 0, 1))
            .setMaximumDate(CalendarDay.from(2030, 11, 31))
            .setCalendarDisplayMode(CalendarMode.MONTHS)
            .commit()

        DataManager.dataList.observe(this, androidx.lifecycle.Observer {
            mScheduleList = it
            putDataOnCalendar()
            if (mCurrentDate != null) {
                refreshList(mCurrentDate!!)
            }
            calView.addDecorators(
                SundayDecorator(),
                SaturdayDecorator(),
                TodayDecorator()
            )
        })



    }

    private fun showDialog(date:CalendarDay) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("※ 경고 ※")
        builder.setMessage("정말로 다 지우겠습니까?")
        builder.setPositiveButton("예") { dialog, which ->
            //                    Toast.makeText(applicationContext, "예를 선택했습니다.", Toast.LENGTH_LONG).show()
            if (mScheduleList != null) {
                val dateString = date.year.toString()+"~"+date.month.toString()+"~"+date.day.toString()
                DataManager.removeDayAllSchedule(dateString)
                refreshList(date)
            }
        }
        builder.setNegativeButton("아니오") { dialog, which ->
//          Toast.makeText(applicationContext, "아니오를 선택했습니다.", Toast.LENGTH_LONG).show()
        }
        builder.show()
    }

    private fun initializeDayListView(calView: MaterialCalendarView) {
        calView.setOnDateChangedListener(OnDateSelectedListener { widget, date, selected ->
            Log.i("kongyi1220", "refreshed recyclerView")
            mCurrentDate = date
            refreshList(date)
            findViewById<Button>(R.id.deleteAllBtn).setOnClickListener {
                showDialog(date)
            }

            findViewById<Button>(R.id.addScheduleBtn).setOnClickListener {
                // 새로 입력 하는 것으로 수정 필요
                val intent = Intent(this, DayActivity::class.java)
                val day: CalendarDay = CalendarDay.from(date.year, date.month, date.day)
                val schedule = Schedule("no_id","${date.year}~${date.month}~${date.day}", "", "", "")
                intent.putExtra("info", schedule)
                startActivity(intent);
            }
        })

        setSwipeToRecyclerView()
    }

    private fun refreshList(date: CalendarDay) {
        val manager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvAdapter = DayListAdapter(
            mScheduleList!!,
            date.year.toString() + "~" + date.month.toString() + "~" + date.day.toString()
        )
        mRecyclerView.layoutManager = manager
        mRecyclerView.adapter = rvAdapter
    }

    private fun setSwipeToRecyclerView() {
        val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPos: Int = viewHolder.adapterPosition
                val toPos: Int = target.adapterPosition
                //                rvAdapter.swapData(fromPos, toPos)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val date =
                    viewHolder.itemView.findViewById<TextView>(R.id.item_date).text.toString()
                val id = viewHolder.itemView.findViewById<TextView>(R.id.item_id).text.toString()
                rvAdapter?.removeData(viewHolder.layoutPosition)
                Log.i("kongyi1220", "removed")
                DataManager.removeSingleSchedule(date, id)
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val icon: Bitmap
                // actionState가 SWIPE 동작일 때 배경을 빨간색으로 칠하는 작업을 수행하도록 함
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val itemView = viewHolder.itemView
                    val height = (itemView.bottom - itemView.top).toFloat()
                    val width = height / 4
                    val paint = Paint()
                    if (dX < 0 || dX > 0) {  // 왼쪽으로 스와이프하는지 확인
                        // 뷰홀더의 백그라운드에 깔아줄 사각형의 크기와 색상을 지정
                        paint.color = Color.parseColor("#22ff0000")
                        val backgroundRight = RectF(
                            itemView.right.toFloat() + dX,
                            itemView.top.toFloat(),
                            itemView.right.toFloat(),
                            itemView.bottom.toFloat()
                        )
                        val backgroundLeft = RectF(
                            itemView.left.toFloat() + dX,
                            itemView.top.toFloat(),
                            itemView.left.toFloat(),
                            itemView.bottom.toFloat()
                        )
                        c.drawRect(backgroundRight, paint)
                        c.drawRect(backgroundLeft, paint)

                        //                        // 휴지통 아이콘과 표시될 위치를 지정하고 비트맵을 그려줌
                        //                        // 비트맵 이미지는 Image Asset 기능으로 추가하고 drawable 폴더에 위치하도록 함
                        //                        icon = BitmapFactory.decodeResource(resources, R.drawable.ic_menu_delete)
                        //                        val iconDst = RectF(itemView.right.toFloat() - 3  - width, itemView.top.toFloat() + width, itemView.right.toFloat() - width, itemView.bottom.toFloat() - width)
                        //                        c.drawBitmap(icon, null, iconDst, null)
                    }
                }

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }

        }

        ItemTouchHelper(itemTouchCallback).attachToRecyclerView(mRecyclerView)
    }

    private fun putDataOnCalendar() {
        val calView = findViewById<MaterialCalendarView>(R.id.calendarView)
        val dayList = ArrayList<CalendarDay>()
        val colorSetHash = HashMap<CalendarDay, MutableSet<String>>()
        calView.removeDecorators()
        Log.i("kongyi1220B", "mScheduleList.size = ${mScheduleList?.size}")
        var current:CalendarDay? = null
        val colorSet = mutableSetOf<String>()
        var day: CalendarDay? = null
        var color: String? = null
        for (schedule in mScheduleList!!) {
            Log.i("kongyi1220B", "show schedule.date = " + schedule.date)
            day = Utils.getDateFromStringToCal(schedule.date)
            color = schedule.color
            if (day != null) {
                if (current == null) {
                    current = day
                }

                if (current == day) {
                    Log.i("kongyi1220B", "Not Clear")
                    colorSet.add(color)
                } else {
                    Log.i("kongyi1220B", "Clear / colorSet.size = " + colorSet.size)
                    colorSetHash[current] = colorSet.toMutableSet()
                    dayList.add(current)
                    colorSet.clear()
                    colorSet.add(color)
                    current = day
                }
            }
        }
        if (day != null) {
            colorSetHash[current!!] = colorSet
            dayList.add(current)
        }

        for (day in dayList) {
            calView.addDecorators(
                colorSetHash[day]?.let { EventDecorator(it, day, this) }
            )
        }
    }
}



class SundayDecorator : DayViewDecorator {
    private val calendar = Calendar.getInstance()
    override fun shouldDecorate(day: CalendarDay): Boolean {
        day.copyTo(calendar)
        val weekDay = calendar[Calendar.DAY_OF_WEEK]
        return weekDay == Calendar.SUNDAY
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(ForegroundColorSpan(Color.RED))
    }
}

class SaturdayDecorator : DayViewDecorator {
    private val calendar = Calendar.getInstance()
    override fun shouldDecorate(day: CalendarDay): Boolean {
        day.copyTo(calendar)
        val weekDay = calendar[Calendar.DAY_OF_WEEK]
        return weekDay == Calendar.SATURDAY
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(ForegroundColorSpan(Color.BLUE))
    }
}

class TodayDecorator : DayViewDecorator {
    private var date: CalendarDay? = CalendarDay.today()
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return date != null && day == date
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(StyleSpan(Typeface.BOLD))
        view.addSpan(RelativeSizeSpan(1.4f))
        view.addSpan(ForegroundColorSpan(Color.rgb(0,180,0)))
    }
}

class EventDecorator(colorSet: MutableSet<String>, day: CalendarDay, context: Activity) :
    DayViewDecorator {
    private var mColorSet: MutableSet<String> = colorSet
    private val mContext = context
    private var mDay = day

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return mDay == day
    }

    override fun decorate(view: DayViewFacade) {
//      Log.i("kongyi1220C", "in decorate mColorSet.size = " + mColorSet.size)
        val colorList = ArrayList<String>()
        if (mColorSet != null) {
            for (color in mColorSet) {
                colorList.add(color)
            }
        }
        Log.i("kongyi1220C", "colorList = $colorList")
        val dot = DotsSpan(mContext, colorList)
        //        val dot = DotSpan(5.0f, color)
        view.addSpan(dot) // 날자밑에 점
        //        view.addSpan(SecondDotSpan())
    }
}

class DotsSpan(context:Context, colorList:ArrayList<String>): LineBackgroundSpan {
    private var mColorList = ArrayList<String>()
    val mContext = context

    init {
        Log.i("kongyi1220D", "init")
        mColorList = colorList
        Log.i("kongyi1220DD", "colorList = " + colorList.toString() + " Object Id = " + mColorList.hashCode())
    }

    override fun drawBackground(
        canvas: Canvas,
        paint: Paint,
        left: Int,
        right: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        lineNumber: Int
    ) {
        val cnt = mColorList.size
        var i = 0
        Log.i("kongyi1220DD", "-->" + mColorList.hashCode())
        for (color in mColorList) {
            i ++
            Log.i("kongyi1220D", "color = " + color)
            when (color) {
                "red" -> paint.color = getColor(mContext, R.color.red)
                "orange" -> paint.color = getColor(mContext, R.color.orange)
                "yellow" -> paint.color = getColor(mContext, R.color.yellow)
                "green" -> paint.color = getColor(mContext, R.color.green)
                "blue" -> paint.color = getColor(mContext, R.color.blue)
                "purple" -> paint.color = getColor(mContext, R.color.purple)
            }
            canvas.drawCircle(((left + right) / 2).toFloat() - 6f*(cnt-1) + 12f*(i-1), bottom + 5.0f, 5.0f, paint)
        }
        paint.color = Color.BLACK
    }
}
