package com.example.mychartviewlibrary.calendar

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.util.SparseArray
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.common.ContextHolder
import com.example.common.data.Schedule
import com.example.model.DataManager
import com.example.mychartviewlibrary.R
import com.example.mychartviewlibrary.calendar.data.CalendarFilter
import com.example.mychartviewlibrary.calendar.data.DateItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

// --> 리사이클러뷰의 재활용성 때문에, 화면에 표시하는 뷰를 다 갖고 있으려고 하면 안된다.
class RecyclerViewAdapterForCalendar(private val context: Context,
                                     val map: SparseArray<ArrayList<Schedule>>,
                                     private var filter: ArrayList<CalendarFilter>,
                                     private val startYear: Int,
                                     private val endYear: Int) : RecyclerView.Adapter<RecyclerViewAdapterForCalendar.ViewHolder>() {
    private val TAG = "RecyclerViewAdapterForCalendar"
    val mContext = context
    var mSelectedView:View? = null
    lateinit var listener: OnDateItemClickListener
    private var items = ArrayList<ArrayList<DateItem>>()

    init {
        for (i in startYear..endYear) {
            for (j in 1..12) {
                items.add(ArrayList<DateItem>())
            }
        }
    }

    fun setFilter(arr: ArrayList<CalendarFilter>) {
        filter = arr
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.calendar_list_item, parent, false))
    }

    @SuppressLint("LongLogTag")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (items[position].size == 0) {
            Log.i("kongyi0516", "onBindViewHolder position = $position")
            items[position] = initMonthView(startYear + position/12, position%12 + 1)
        }
        holder.setData(position, items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("UseCompatLoadingForDrawables")
        fun setData(pos:Int, dataViewList: ArrayList<DateItem>) {
            Log.i("kongyi0515-3", "setData()")
            Log.i("kongyi0515-5", "filter.size = ${filter.size}")
            Log.i("kongyi0515-5", "filter = ${filter[0]}")

            val frame = itemView.rootView as LinearLayout
            val frameChildCount = frame.childCount
            var cnt = 0
            for (i in 0 until frameChildCount) {
                val row = frame.getChildAt(i) as LinearLayout
                val rowChildCount = row.childCount
                for (j in 0 until rowChildCount) {
                    val item = row.getChildAt(j) as LinearLayout
                    //item.background = getDrawable(mContext, R.drawable.ic_launcher_background)
                    val view = dataViewList[cnt].view as ConstraintLayout
                    val textView = view.getChildAt(0) as TextView
                    val linearLayout = view.getChildAt(1) as LinearLayout
                    textView.text = dataViewList[cnt].text
                    view.layoutParams = ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.MATCH_PARENT)
                    textView.gravity = Gravity.CENTER
                    item.removeAllViews()
                    addScheduleCircleAtDate(dataViewList, cnt, linearLayout)
                    if (view.parent != null) {
                        (view.parent as ViewGroup).removeAllViews()
                    }
                    item.addView(view)
                    markWeekendColor(cnt, textView)
                    textView.visibility = View.VISIBLE
                    //getTextViewByKey.append(arr[cnt].getKey(), textView)

                    markToday(dataViewList, cnt, textView)

                    if (dataViewList[cnt].role == 1) {
                        val value = dataViewList[cnt]
                        item.getChildAt(0).setOnClickListener {
                            val previousView = mSelectedView
                            if (previousView != null) {
                                previousView.background = null
                            }
                            mSelectedView = it
                            val position = adapterPosition
                            if (listener != null) {
                                Log.i("kongyi0505", "arr[cnt] = [${value}]")
                                listener.onItemClick(this, it, position, value)
                            }
                            it.background = mContext.getDrawable(R.drawable.circle_light_yellow)
                        }
                        val dragListener = DragListener(value)
                        item.getChildAt(0).setOnDragListener(dragListener)
                    }
                    cnt ++
                }
            }
            //itemView.findViewById<TextView>(R.id.text).text = item
        }


    }

    private fun markWeekendColor(cnt: Int, textView: TextView) {
        if (cnt > 6 && cnt % 7 == 6)
            textView.setTextColor(ContextCompat.getColor(mContext, R.color.blue))
        if (cnt > 6 && cnt % 7 == 0)
            textView.setTextColor(ContextCompat.getColor(mContext, R.color.red))
    }

    private fun markToday(arr: ArrayList<DateItem>,
                          cnt: Int,
                          textView: TextView) {
        val cal = Calendar.getInstance()
        cal.timeInMillis = System.currentTimeMillis()
        if (cal.get(Calendar.YEAR) == arr[cnt].year &&
                cal.get(Calendar.MONTH) == arr[cnt].month &&
                cal.get(Calendar.DATE) == arr[cnt].date) {
            Log.i("kongyi0504", "markToday")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                textView.setTextColor(context.getColor(R.color.green_today))
                textView.textSize = 20.0f
            }
        }
    }

    // 한 날짜에 같은 스케쥴이 두개 씩 들어가는 이슈 있음.
    @SuppressLint("LongLogTag")
    private fun addScheduleCircleAtDate(
        arr: ArrayList<DateItem>,
        cnt: Int,
        linearLayout: LinearLayout
    ) {
        val list = map[arr[cnt].getKey()]
        linearLayout.removeAllViews()
        if (list != null) {
//
//            Log.i("kongyi0515-5", "filter.size = ${filter.size}")
//            Log.i("kongyi0515-5", "filter.mode.size = ${filter[0].mode.size}")
//            Log.i("kongyi0515-5", "filter.colorFilter.size = ${filter[0].colorFilter.size}")

            for (schedule in list) {
                var condition = true
                if (filter.size > 0 && filter[0].mode.size > 0) {
                    condition = if (filter[0].mode[0] == 1) {
                        schedule.color in filter[0].colorFilter &&
                                (schedule.content.contains(filter[0].keyword[0]) || schedule.title.contains(
                                    filter[0].keyword[0]))
                    } else {
                        schedule.color in filter[0].colorFilter ||
                                (schedule.content.contains(filter[0].keyword[0]) || schedule.title.contains(
                                    filter[0].keyword[0]))
                    }
                } else {
                    Log.i("kongyi0515-5", "There is no filter mode value!!")
                }

                if (condition) {
                    val img = ImageView(context)
                    img.background = context.getDrawable(R.drawable.circle_black)
                    img.layoutParams = LinearLayout.LayoutParams(
                        Utils.convertDPtoPX(context, 5),
                        Utils.convertDPtoPX(context, 5)
                    )
                    when {
                        schedule.color == "blue" -> img.backgroundTintList =
                            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.blue))
                        schedule.color == "purple" -> img.backgroundTintList =
                            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.purple))
                        schedule.color == "green" -> img.backgroundTintList =
                            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.green))
                        schedule.color == "black" -> img.backgroundTintList =
                            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.black))
                        schedule.color == "orange" -> img.backgroundTintList =
                            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.orange))
                        schedule.color == "yellow" -> img.backgroundTintList =
                            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.yellow))
                        schedule.color == "red" -> img.backgroundTintList =
                            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red))
                    }
                    linearLayout.addView(img)
                }
            }
        }
    }

    var isDropped: Boolean = false

    fun setDropState(state:Boolean) {
        isDropped = state
    }

    inner class DragListener(private val targetDateItem: DateItem) : View.OnDragListener {
        @SuppressLint("UseCompatLoadingForDrawables")
        private var mPreviousBackground: Drawable? = null
        @RequiresApi(Build.VERSION_CODES.S)
        override fun onDrag(v: View, event: DragEvent): Boolean {
            // 이벤트 시작
            val itemViewFromList = event.localState as View
            val itemId = itemViewFromList.findViewById<TextView>(R.id.item_id).text.toString()
            val itemDate = itemViewFromList.findViewById<TextView>(R.id.item_date).text.toString()
            val itemTitle = itemViewFromList.findViewById<TextView>(R.id.item_title).text.toString()
            val itemContent = itemViewFromList.findViewById<TextView>(R.id.item_content).text.toString()
            val itemColor = itemViewFromList.findViewById<TextView>(R.id.item_color).text.toString()
            val schedule = Schedule(itemId, itemDate, itemTitle, itemContent, itemColor)

            Utils.getMyDateFromStringToDateItem(itemDate)?. let { fromDate ->
                if (targetDateItem.year == fromDate.year &&
                        targetDateItem.month == fromDate.month &&
                        targetDateItem.date == fromDate.date) {
                    return true
                }
            }
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> { // 각 리스너의 드래그 앤 드롭 시작 상태 (3번 call됨)
                    Log.i("kongyi0424", "ACTION_DRAG_STARTED")
                }
                DragEvent.ACTION_DRAG_ENTERED -> { // 이미지가 들어옴
                    Log.i("kongyi0424", "ACTION_DRAG_ENTERED")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        v.setBackgroundColor(context.getColor(R.color.colorAccent))
                    }
                }
                DragEvent.ACTION_DRAG_EXITED -> { // 이미지가 나감
                    Log.i("kongyi0424", "ACTION_DRAG_EXITED")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (mPreviousBackground != null) {
                            v.background = mPreviousBackground!!
                        } else {
                            v.setBackgroundColor(0)
                        }
                    }
                }
                DragEvent.ACTION_DROP -> {
                    isDropped = true
                    Log.i("kongyi0517", "ACTION_DROP")
//                    val itemViewFromList = event.localState as View
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        v.setBackgroundColor(0)
                    }
                    moveSchedule(schedule, Utils.getDateFromYearMonthDay(targetDateItem))
//                    val itemViewParent = itemViewFromList.parent as ViewGroup
//                    val positionAtList = itemViewParent.indexOfChild(itemViewFromList)
                }
                DragEvent.ACTION_DRAG_ENDED -> { // 각 리스너의 드래그 앤 드롭 종료 상태 (3번 call됨)
                    Log.i("kongyi0517", "ACTION_DRAG_ENDED | dropCount = $isDropped")
                    if (isDropped) {
                        itemViewFromList.visibility = View.INVISIBLE
                    } else {
                        itemViewFromList.visibility = View.VISIBLE
                    }
                }
            }
            return true
        }

        private fun moveSchedule(fromSchedule:Schedule, toDate:String) {
            CoroutineScope(Dispatchers.Default).launch {
                DataManager.removeSingleSchedule(
                    "id_list", fromSchedule.date, fromSchedule.id
                )
                DataManager.putSingleSchedule(
                    "id_list", toDate,
                    fromSchedule.title,
                    fromSchedule.content,
                    fromSchedule.color,
                    fromSchedule.id
                )
                val from_str =
                    "${fromSchedule.date}, ${fromSchedule.title}, ${fromSchedule.content}, ${fromSchedule.color}, ${fromSchedule.id}"
                val to_str =
                    "${toDate}, ${fromSchedule.title}, ${fromSchedule.content}, ${fromSchedule.color}, ${fromSchedule.id}"

                DataManager.putSingleHistory(
                    context,
                    "cal-schedule-modify",
                    "from: $from_str",
                    "to: $to_str",
                    ContextHolder.getPhoneNumber()
                )
            }
        }
    }

    fun initMonthView(year: Int, month: Int): ArrayList<DateItem> {
        val monthForCalendarLib = month - 1
        return getMonthData(year, monthForCalendarLib)
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

}

