package com.example.mynotepad

import com.prolificinteractive.materialcalendarview.CalendarDay

object Utils {
    fun getDateFromStringToCal(str:String): CalendarDay? {
        if (str.indexOf("~") != -1) {
            val dayOfMonth = str.substring(str.lastIndexOf("~")+1)
            val year = str.substring(0, str.indexOf("~"))
            val rest = str.substring(str.indexOf("~") + 1, str.length)
            val month = rest.substring(0, rest.indexOf("~"))
            val day: CalendarDay = CalendarDay.from(year.toInt(), month.toInt(), dayOfMonth.toInt())
            return day
        }
        return null
    }

    fun convDBdateToShown(str:String): String {
        if (str.indexOf("~") != -1) {
            val dayOfMonth = str.substring(str.lastIndexOf("~")+1)
            val year = str.substring(0, str.indexOf("~"))
            val rest = str.substring(str.indexOf("~") + 1, str.length)
            val month = rest.substring(0, rest.indexOf("~"))
            return year + "/" + addFront0((month.toInt()+1).toString()) + "/" + addFront0(dayOfMonth)
        }
        return ""
    }

    fun addFront0(str:String): String {
        if (str.length == 1) {
            return "0$str"
        }
        return str
    }
}