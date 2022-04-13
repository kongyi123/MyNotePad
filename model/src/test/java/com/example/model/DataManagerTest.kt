package com.example.model

import com.prolificinteractive.materialcalendarview.CalendarDay
import org.junit.Test

import org.junit.Assert.*
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

class DataManagerTest {
    @Test
    fun testGetOnlySubjectLineNumber() {
        val cal =  mock(CalendarDay::class.java)

        `when`(cal.calendar.get(0)).thenReturn(
            1
        )
        assertEquals(cal.calendar.get(0), 1)


//        val day: CalendarDay = CalendarDay.from(2022, 1, 22)
//        assertEquals(day, Utils.getDateFromStringToCal("2022~1~22"))
    }
}