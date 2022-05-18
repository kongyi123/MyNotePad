package com.example.mychartviewlibrary.calendar.list

import androidx.recyclerview.widget.RecyclerView

interface ITask {
    fun doTaskOnSwiped(viewHolder: RecyclerView.ViewHolder)
}