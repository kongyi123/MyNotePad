package com.example.paperweight

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.model.DataManager
import com.example.common.Utils
import com.example.common.data.Schedule
import com.example.paperweight.list.WeightListAdapter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class PaperWeightActivity : AppCompatActivity() {
    lateinit var weightListAdapter: WeightListAdapter
    val list = mutableListOf<WeightPaperData>()
    val exsisted = HashMap<String, Schedule>()
    var today = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paper_weight)
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        vibrator.cancel(); //<- 중단 시키기 ?? 이게 안먹는것처럼 보인다. 문진 액티비티로 와도 진동이 안멈춤.
        // 대신 화면을 켰다가 끄면 멈추는 것으로 보임

        DataManager.getAllScheduleData("pid_list")
        DataManager.dataList.observe(this, {
            initOthers()
            initRecycler()
        })
    }

    private fun initOthers() {
        val cal = Calendar.getInstance()
        cal.timeInMillis = System.currentTimeMillis()
        val df = SimpleDateFormat("MM월 dd일 (E)", Locale.KOREAN)
        findViewById<TextView>(R.id.date_text_view).text = df.format(Date())
        findViewById<Button>(R.id.paper_weight_activity_cancel_btn).setOnClickListener { finish() }
        findViewById<Button>(R.id.paper_weight_activity_confirm_btn).setOnClickListener { update() }
    }

    private fun initRecycler() {
        weightListAdapter = WeightListAdapter(this)
        findViewById<RecyclerView>(R.id.paper_recyclerview).adapter = weightListAdapter
        list.clear()
        list.apply {
            Log.i("kongyi444", "datas.apply applied")
            val cal = Calendar.getInstance()
            cal.timeInMillis = System.currentTimeMillis()
            val dateOfToday = Utils.getDateFromCalToString(cal)
            Log.d("kongyi444", "date = $dateOfToday")
            val listInDate = DataManager.getScheduleDataInDate(dateOfToday)
            putCard(listInDate, "green", "기록", "오늘 한 일 기록하자", " *3가지 키워드만 쓰자")
            putCard(listInDate, "blue", "업무/익스", "한 업무를 기록하자", " *키워드-진행위치 형식으로")
            putCard(listInDate, "purple", "골프", "골프 쳤니?", " *깨달은 점도 비고란에")
            putCard(listInDate, "yellow", "재택근무", "재택근무했니?", "")
            Log.i("kongyi1220", "datas.apply applied / datas = ${list}")

            weightListAdapter.datas = list
            weightListAdapter.notifyDataSetChanged()
        }
    }

    private fun update() {
        Log.i("kongyi555", "update()")
        singleUpdate(0, "기록")
        singleUpdate(1, "업무/익스")
        singleUpdate(2, "골프")
        singleUpdate(3, "재택근무")
        finish()
    }

    private fun singleUpdate(idx:Int, title:String) {
        Log.i("kongyi555", "idx = $idx, title = $title isYes = ${weightListAdapter.datas[idx].isYes}")
        if (weightListAdapter.datas[idx].isYes) {
            val ex = exsisted[title]
            if (ex != null) {
                val sc = weightListAdapter.datas[idx]
                DataManager.putSingleSchedule(
                    "pid_list",
                    ex!!.date,
                    sc.paper_weight_item_title,
                    sc.paper_weight_comment,
                    sc.paper_weight_color_circle,
                    ex.id
                )
            } else {
                val sc = weightListAdapter.datas[idx]
                val cal = Calendar.getInstance()
                cal.timeInMillis = System.currentTimeMillis()
                val dateOfToday = Utils.getDateFromCalToString(cal)
                DataManager.putSingleSchedule(
                    "pid_list",
                    dateOfToday,
                    sc.paper_weight_item_title,
                    sc.paper_weight_comment,
                    sc.paper_weight_color_circle,
                    "no_id"
                )
            }
        } else if (exsisted[title] != null) {
            val sc = exsisted[title]
            DataManager.removeSingleSchedule("pid_list", sc!!.date, sc.id)
        }
    }

    private fun MutableList<WeightPaperData>.putCard(listInDate: ArrayList<Schedule>, color: String, title: String, main: String, sub: String) {
        var isRecord = false
        var comment = ""
        for (node in listInDate) {
            Log.i("kongyi444", "node = ${node.toString()}")
            if (node.title == title) {
                isRecord = true
                comment = node.content
                exsisted[title] = node
                break
            }
        }
        add(
            WeightPaperData(
                paper_weight_color_circle = color,
                paper_weight_item_title = title,
                paper_weight_question_tv = main,
                paper_weight_sub_tv = sub,
                isYes = isRecord,
                paper_weight_comment = comment
            )
        )
    }

}