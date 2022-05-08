package com.example.mychartviewlibrary.calendar

import android.content.Context
import com.example.mychartviewlibrary.calendar.data.DateItem
import java.security.MessageDigest
import java.util.*
import kotlin.math.roundToInt

object Utils {

    fun getMyDateFromStringToDateItem(str:String): DateItem? {
        if (str.indexOf("~") != -1) {
            val dayOfMonth = str.substring(str.lastIndexOf("~")+1)
            val year = str.substring(0, str.indexOf("~"))
            val rest = str.substring(str.indexOf("~") + 1, str.length)
            val month = rest.substring(0, rest.indexOf("~"))
            return DateItem(year.toInt(), month.toInt(), dayOfMonth.toInt())
        }
        return null
    }

    fun convertDPtoPX(context: Context, dp:Int):Int {
        val density = context.resources.displayMetrics.density;
        return ((dp.toFloat()) * density).roundToInt();
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

    fun getDateFromYearMonthDay(dateItem: DateItem):String {
        return "${dateItem.year}~${addFront0(dateItem.month.toString())}~${addFront0(dateItem.date.toString())}"
    }

    fun addFront0(str:String): String {
        if (str.length == 1) {
            return "0$str"
        }
        return str
    }

    fun isMonth2Char(str:String): Boolean {
        if (str.indexOf("~") != -1) {
            val dayOfMonth = str.substring(str.lastIndexOf("~")+1)
            val year = str.substring(0, str.indexOf("~"))
            val rest = str.substring(str.indexOf("~") + 1, str.length)
            val month = rest.substring(0, rest.indexOf("~"))
            return month.length == 2
        }
        return false
    }

    fun isDay2Char(str:String): Boolean {
        if (str.indexOf("~") != -1) {
            val dayOfMonth = str.substring(str.lastIndexOf("~")+1)
            val year = str.substring(0, str.indexOf("~"))
            val rest = str.substring(str.indexOf("~") + 1, str.length)
            val month = rest.substring(0, rest.indexOf("~"))
            return dayOfMonth.length == 2
        }
        return false
    }

    fun getDateFromCalToString(cal:Calendar):String {
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DATE)
        if (month < 10) {
            return "$year~0$month~$day"
        }
        return "$year~$month~$day"
    }


}