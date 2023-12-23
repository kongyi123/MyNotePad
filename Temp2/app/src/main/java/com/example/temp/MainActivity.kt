package com.example.temp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.temp.ui.theme.BoardAdpt
import com.example.temp.ui.theme.CalendarAdpt
//import com.example.temp.ui.theme.TempTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val vpage=findViewById<ViewPager2>(R.id.viewpager)
        val claAdpt = CalendarAdpt(applicationContext)
        vpage.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        vpage.adapter = claAdpt
        vpage.currentItem = 1200
        vpage.offscreenPageLimit = 2


    }
}