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

class DayListAdapter(scheduleList: ArrayList<Schedule>, date:String, private val listener: OnScheduleItemClickListener) : RecyclerView.Adapter<ViewHolder>() {
    private val mDayScheduleList = ArrayList<Schedule>()
    private var mContext: Context? = null
    init {
        mDayScheduleList.clear()
        Log.i("kongyi0508", "date = [${date}]")
        for (schedule in scheduleList) {
            Log.i("kongyi0508", "date = [${date}] / schedule.date = [${schedule.date}]")
            if (date.compareTo(schedule.date) == 0) {
                Log.i("kongyi0508", "in adapter = " + schedule.title + " " + schedule.content)
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
                "red" -> holder.colorCircle.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(mContext!!, R.color.red))
                "orange" -> holder.colorCircle.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(mContext!!, R.color.orange))
                "yellow" -> holder.colorCircle.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(mContext!!, R.color.yellow))
                "green" -> holder.colorCircle.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(mContext!!, R.color.green))
                "blue" -> holder.colorCircle.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(mContext!!, R.color.blue))
                "purple" -> holder.colorCircle.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(mContext!!, R.color.purple))
            }
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