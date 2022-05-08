package com.example.mychartviewlibrary.calendar.data

import android.view.LayoutInflater
import android.view.View
import com.example.common.ContextHolder
import com.example.mychartviewlibrary.R

// ContextHolder?
data class DateItem(
    val year: Int = 0,
    val month: Int = 0,
    val date: Int = 0,
    val role: Int = 0, // 0이면 문자역할만, 1이면 날짜 역할
    val text: String = "", // LayoutInflater.from(parent.context).inflate(R.layout.calendar_list_item, parent, false))
    val view: View = LayoutInflater.from(ContextHolder.getContext()).inflate(R.layout.calendar_date_item, null,false)
) {
    fun getKey():Int {
        return year*10000 + month*100 + date
    }
}