package com.example.temp.ui.theme

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.temp.R
import java.util.Calendar

//
class BoardAdpt(var context:Context, var Data:ArrayList<Day>): RecyclerView.Adapter<BoardAdpt.BoardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ltem, parent, false)
        return BoardViewHolder(view)
    }

    override fun onBindViewHolder(holder: BoardViewHolder, position: Int) {

        holder.tv_time.text = Data[position].day
    }
    override fun getItemCount(): Int {
        return Data.count()
    }
    inner class BoardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv_time = itemView.findViewById<TextView>(R.id.tv_time)

    }
}

