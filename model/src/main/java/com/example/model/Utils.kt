package com.example.model

import com.prolificinteractive.materialcalendarview.CalendarDay
import java.security.MessageDigest
import java.util.*

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

    fun isMonth2Char(str:String): Boolean {
        if (str.indexOf("~") != -1) {
            val dayOfMonth = str.substring(str.lastIndexOf("~")+1)
            val year = str.substring(0, str.indexOf("~"))
            val rest = str.substring(str.indexOf("~") + 1, str.length)
            val month = rest.substring(0, rest.indexOf("~"))
            val day: CalendarDay = CalendarDay.from(year.toInt(), month.toInt(), dayOfMonth.toInt())
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
            val day: CalendarDay = CalendarDay.from(year.toInt(), month.toInt(), dayOfMonth.toInt())
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

    /**
     * SHA-256으로 해싱하는 메소드
     *
     * @param bytes
     * @return
     * @throws NoSuchAlgorithmException
     */
    fun sha256(msg:String) : ByteArray {
        val md = MessageDigest.getInstance("SHA-256")

        md.update(msg.toByteArray());

        return md.digest();
    }

    /**
     * 바이트를 헥사값으로 변환한다, type 1
     *
     * @param bytes
     * @return
     */
    fun bytesToHex1(bytes:ByteArray):String {
        val builder = StringBuilder();
        for (b in bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

}