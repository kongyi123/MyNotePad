package com.example.personalcalendar.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.example.personalcalendar.R
import com.example.model.DataManager
import com.example.model.DataManager.putSingleHistory
import com.example.common.CommonUtils
import com.example.common.data.Schedule


// 브랜치 테스트 수정.
class DayActivity : AppCompatActivity() {
    private lateinit var inputButtonView: Button
    private lateinit var closeButtonView: Button
    private lateinit var contentView: EditText
    private lateinit var titleView: EditText
    private lateinit var dateView: DatePicker
    private var mSelectedColor:String = ""
    private var isNew = false
    private var mSchedule: Schedule? = null
    private lateinit var mPhoneNumber:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_day)
        inputButtonView = findViewById(R.id.inputBtn)
        closeButtonView = findViewById(R.id.closeBtn)
        contentView = findViewById(R.id.content)
        titleView = findViewById(R.id.title)
        dateView = findViewById(R.id.datePicker)
        findViewById<RadioButton>(R.id.radio_button_copy).setOnClickListener {
            setRadioButtonCopy()
        }
        findViewById<RadioButton>(R.id.radio_button_modify). setOnClickListener {
            setRadioButtonModify()
        }
//
//        DataManager.hList.observe(this, androidx.lifecycle.Observer {
//            Log.i("kongyi1220TT", "hList is updated.")
////            init()
//            var content = "none"
//            if (it.get(it.lastIndex).arg2 == "access") {
//
//                content = "번호 = ${it.get(it.lastIndex).arg1} 행위 = ${it.get(it.lastIndex).arg2}" +
//                        " 대상 = ${it.get(it.lastIndex).arg3} 주체 = ${it.get(it.lastIndex).arg4}"
//
//            } else if (it.get(it.lastIndex).arg2 == "pcal-schedule-new") {
//                content = "번호 = ${it.get(it.lastIndex).arg1} 행위 = ${it.get(it.lastIndex).arg2}" +
//                        " arg3 = ${it.get(it.lastIndex).arg3} arg4 = ${it.get(it.lastIndex).arg4}" +
//                        " arg5 = ${it.get(it.lastIndex).arg5}"
//            }
//
//            MyNotification.doNotify(this, content)
//        })

        mSchedule = intent.getSerializableExtra("info") as? Schedule
        Log.i("kongyi1220", "hey!!! title = " + mSchedule?.title)
        titleView.setText(mSchedule?.title)
        contentView.setText(mSchedule?.content)

        if (mSchedule != null) {
            val day = CommonUtils.getDateFromStringToCal(mSchedule!!.date)
            dateView.init(day!!.year, day.month, day.day, null)
        }

        if (mSchedule?.id == "no_id") {
            setRadioButtonCopy()
        }

        inputButtonView.setOnClickListener {
            setInput()
        }
        closeButtonView.setOnClickListener {
            onClickClose()
        }

//        deleteButtonView.setOnClickListener {
//            date = "${ dateView.year }~${ dateView.month }~${ dateView.dayOfMonth }"
//            Log.i("kongyi1220", "removed")
//            DataManager.removeSingleSchedule(date)
//            onClickClose()
//        }

        when (mSchedule?.color) {
            "red" -> {
                findViewById<RadioButton>(R.id.radio_button_red).isChecked = true
                mSelectedColor = "red"
            }
            "orange" -> {
                findViewById<RadioButton>(R.id.radio_button_orange).isChecked = true
                mSelectedColor = "orange"
            }
            "yellow" -> {
                findViewById<RadioButton>(R.id.radio_button_yellow).isChecked = true
                mSelectedColor = "yellow"
            }
            "green" -> {
                findViewById<RadioButton>(R.id.radio_button_green).isChecked = true
                mSelectedColor = "green"
            }
            "blue" -> {
                findViewById<RadioButton>(R.id.radio_button_blue).isChecked = true
                mSelectedColor = "blue"
            }
            "purple" -> {
                findViewById<RadioButton>(R.id.radio_button_purple).isChecked = true
                mSelectedColor = "purple"
            }
        }

        val radioGroup = findViewById<RadioGroup>(R.id.radio_group)
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radio_button_red -> mSelectedColor = "red"
                R.id.radio_button_orange -> mSelectedColor = "orange"
                R.id.radio_button_yellow -> mSelectedColor = "yellow"
                R.id.radio_button_green -> mSelectedColor = "green"
                R.id.radio_button_blue -> mSelectedColor = "blue"
                R.id.radio_button_purple -> mSelectedColor = "purple"
            }
        }

        mPhoneNumber = DataManager.getLineNumber(this, this)
    }

    private fun setInput() {
        var date = "${dateView.year}~${CommonUtils.addFront0(dateView.month.toString())}~${CommonUtils.addFront0(dateView.dayOfMonth.toString())}"
        if (isNew) {
            mSchedule?.id = "no_id"
            DataManager.putSingleSchedule(
                "pid_list",
                date,
                titleView.text.toString(),
                contentView.text.toString(),
                mSelectedColor,
                mSchedule!!.id
            )
            val str = "$date, ${titleView.text}, ${contentView.text}, $mSelectedColor, ${
                CommonUtils.bytesToHex1(
                    CommonUtils.sha256(date+titleView.text+contentView.text))}"

            putSingleHistory(this, "pcal-schedule-new", "content: $str", mPhoneNumber)
        } else {
            DataManager.removeSingleSchedule("pid_list", mSchedule!!.date, mSchedule!!.id)
            DataManager.putSingleSchedule(
                "pid_list",
                date,
                titleView.text.toString(),
                contentView.text.toString(),
                mSelectedColor,
                mSchedule!!.id
            )
            val from_str = "${mSchedule!!.date}, ${mSchedule!!.title}, ${mSchedule!!.content}, ${mSchedule!!.color}, ${mSchedule!!.id}"
            val to_str = "$date, ${titleView.text}, ${contentView.text}, $mSelectedColor, ${mSchedule!!.id}"

            putSingleHistory(this, "pcal-schedule-modify", "from: $from_str", "to: $to_str", mPhoneNumber)
        }
        //conditionRef.setValue(editText.text.toString())
        onClickClose()
    }

    private fun setRadioButtonCopy() {
        isNew = true
        Log.i("kongyi1220A", "setRadioButtonCopy")
        findViewById<RadioButton>(R.id.radio_button_copy).isChecked = true
        findViewById<RadioButton>(R.id.radio_button_modify).isChecked = false
    }

    private fun setRadioButtonModify() {
        isNew = false
        Log.i("kongyi1220A", "setRadioButtonModify")
        findViewById<RadioButton>(R.id.radio_button_copy).isChecked = false
        findViewById<RadioButton>(R.id.radio_button_modify).isChecked = true
    }


    override fun onPause() {
        super.onPause()
        overridePendingTransition(0, 0)
    }

    fun onClickClose() {
        finish()
    }

    override fun onBackPressed() {
        setInput()
        Toast.makeText(this, "saved", Toast.LENGTH_SHORT).show()
        super.onBackPressed()
    }
}