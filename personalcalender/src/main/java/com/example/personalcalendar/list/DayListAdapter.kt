package com.example.personalcalendar.list

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.personalcalendar.R
import com.example.common.data.Schedule
import java.util.ArrayList

class DayListAdapter(scheduleList: ArrayList<Schedule>, date:String) : RecyclerView.Adapter<ViewHolder>() {
    private val mDayScheduleList = ArrayList<Schedule>()

    init {
        mDayScheduleList.clear()
        for (schedule in scheduleList) {
            if (date.compareTo(schedule.date) == 0) {
                Log.i("kongyi1220", "in adapter = " + schedule.title + " " + schedule.content)
                mDayScheduleList.add(schedule)
            }
        }
        Log.i("kongyi1220", "in adapter size = " + mDayScheduleList.size)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val context = parent.context
        val inflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title.text = mDayScheduleList[position].title
        holder.content.text = mDayScheduleList[position].content
        holder.date.text = mDayScheduleList[position].date
        holder.id.text = mDayScheduleList[position].id
        holder.color.text = mDayScheduleList[position].color
        when (holder.color.text) {
            "red" -> holder.colorCircle.setBackgroundResource(R.drawable.circle_red)
            "orange" -> holder.colorCircle.setBackgroundResource(R.drawable.circle_orange)
            "yellow" -> holder.colorCircle.setBackgroundResource(R.drawable.circle_yellow)
            "green" -> holder.colorCircle.setBackgroundResource(R.drawable.circle_green)
            "blue" -> holder.colorCircle.setBackgroundResource(R.drawable.circle_blue)
            "purple" -> holder.colorCircle.setBackgroundResource(R.drawable.circle_purple)
        }
    }

    override fun getItemCount(): Int {
        return mDayScheduleList.size
    }

    // 뷰홀더 포지션을 받아 그 위치의 데이터를 삭제하고 notifyItemRemoved로 어댑터에 갱신명령을 전달
    fun removeData(position: Int) {
        mDayScheduleList.removeAt(position)
        notifyItemRemoved(position)
    }

//    // 두 개의 뷰홀더 포지션을 받아 Collections.swap으로 첫번째 위치와 두번째 위치의 데이터를 교환
//    fun swapData(fromPos: Int, toPos: Int) {
//        Collections.swap(dataSet, fromPos, toPos)
//        notifyItemMoved(fromPos, toPos)
//    }

}