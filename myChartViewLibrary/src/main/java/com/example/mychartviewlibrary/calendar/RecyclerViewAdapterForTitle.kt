package com.example.mychartviewlibrary.calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mychartviewlibrary.R
import java.util.ArrayList

class RecyclerViewAdapterForTitle(startYear:Int, endYear:Int) : RecyclerView.Adapter<RecyclerViewAdapterForTitle.ViewHolder>() {
    private var items = ArrayList<String>()

    init {
        for (year in startYear..endYear) {
            for (month in 1..12) {
                items.add("${month}월 $year")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.calendar_title, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setData(items[position])
    }
    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun setData(title: String) {
            (itemView as TextView).text = title
        }
    }

}