package com.example.mychartviewlibrary.calendar.list

import android.content.Context
import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.common.data.Schedule
import com.example.mychartviewlibrary.R
import com.example.mychartviewlibrary.calendar.data.CalendarFilter

class DayListAdapter(scheduleList: ArrayList<Schedule>,
                     date:String,
                     private val listener: OnScheduleItemClickListener,
                     filter: ArrayList<CalendarFilter>
) : RecyclerView.Adapter<ViewHolder>() {
    private val TAG = "DayListAdapter"
    private val mDayScheduleList = ArrayList<Schedule>()
    private var mContext: Context? = null
    init {
        mDayScheduleList.clear()
        for (schedule in scheduleList) {
            if (date.compareTo(schedule.date) == 0) {
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
                    Log.i(TAG, "There is no filter mode value!!")
                }

                if (condition) {
                    mDayScheduleList.add(schedule)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val context = parent.context
        mContext = context
        val inflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.calendar_schedule_list_item, parent, false)
        return ViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title.text = mDayScheduleList[position].title
        holder.content.text = mDayScheduleList[position].content
        holder.date.text = mDayScheduleList[position].date
        holder.id.text = mDayScheduleList[position].id
        holder.color.text = mDayScheduleList[position].color
        holder.colorCircle.setBackgroundResource(R.drawable.circle_black)
        mContext?.let {
            when (holder.color.text) {
                "red" -> holder.colorCircle.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(mContext!!, R.color.red))
                "orange" -> holder.colorCircle.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(mContext!!, R.color.orange))
                "yellow" -> holder.colorCircle.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(mContext!!, R.color.yellow))
                "green" -> holder.colorCircle.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(mContext!!, R.color.green))
                "blue" -> holder.colorCircle.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(mContext!!, R.color.blue))
                "purple" -> holder.colorCircle.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(mContext!!, R.color.purple))
            }
        }
    }

    override fun getItemCount(): Int {
        return mDayScheduleList.size
    }
}