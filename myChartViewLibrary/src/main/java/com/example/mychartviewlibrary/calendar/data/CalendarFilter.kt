package com.example.mychartviewlibrary.calendar.data

import java.util.ArrayList

data class CalendarFilter(
    val colorFilter: ArrayList<String>,
    val keyword: ArrayList<String>,
    val mode: ArrayList<Int>  // OR(0), AND(1)
)