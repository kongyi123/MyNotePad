package com.example.mychartviewlibrary.calendar

import android.view.View
import com.example.mychartviewlibrary.calendar.data.DateItem

interface OnDateItemClickListener {
    fun onItemClick(holder: RecyclerViewAdapterForCalendar.ViewHolder, view: View, position: Int, dateItem: DateItem)
}