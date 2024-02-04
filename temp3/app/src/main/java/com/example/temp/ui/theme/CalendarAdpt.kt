package com.example.temp.ui.theme

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.temp.R
import java.util.Calendar

class CalendarAdpt(private var context:Context):RecyclerView.Adapter<CalendarAdpt.ViewHolder>()
{
    //

    val cal=Calendar.getInstance()
    val Dates = ArrayList<Day>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=LayoutInflater.from(context).inflate(R.layout.calendar,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        holder.setCalendar(position)
       // Log.d("온바인드 함수","${position}")
    }
    override fun getItemCount(): Int {
        return 2400  //Integer.MAX_VALUE
    }

    //===============================================================
   inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView)
    {
        var tv=itemView.findViewById<TextView>(R.id.textView)


        public  fun setCalendar(position:Int)
        {
            var Year=cal.get(Calendar.YEAR)+((position+cal.get(Calendar.MONTH))/12-100)
            var Moon=((position+cal.get(Calendar.MONTH))%12)
            val recycle=itemView.findViewById<RecyclerView>(R.id.calendarRecycle)
            val boardAdapter = BoardAdpt(context,Dates)
            tv.text="${Year}년 ${Moon+1}월"
            getMonthData(Year,Moon)
            recycle.layoutManager = GridLayoutManager(context,7)
            boardAdapter.notifyDataSetChanged()
            recycle.adapter = boardAdapter

        }

    }

    public fun getMonthData(Year:Int,Month:Int) {
         Dates.clear()
        var cal = Calendar.getInstance()
        cal.set(Year,Month,1)
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val dayOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        Dates.add(Day("일")); Dates.add(Day("월")); Dates.add(Day("화")); Dates.add(Day("수")); Dates.add(Day("목")); Dates.add(Day("금")); Dates.add(Day("토"))
        for (i in 1 until dayOfWeek) {
            Dates.add(Day(""))
        }
        var cnt = 0
        for (i in 1..dayOfMonth) {
            cnt++
            Dates.add(Day(cnt.toString(),cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)))
        }
//       for (i in 0..30) {
//        dates.add(DateItem(text = ""))
//       }


    }
}