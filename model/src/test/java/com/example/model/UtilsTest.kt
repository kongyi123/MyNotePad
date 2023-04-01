package com.example.model

import com.example.mynotepad.Utils
import com.prolificinteractive.materialcalendarview.CalendarDay
import org.junit.Test

import org.junit.Assert.*

class UtilsTest {
    @Test
    fun testGetDateFromStringToCal() {
        val day: CalendarDay = CalendarDay.from(2022, 1, 22)
        assertEquals(day, com.example.mynotepad.Utils.getDateFromStringToCal("2022~1~22"))
    }
}