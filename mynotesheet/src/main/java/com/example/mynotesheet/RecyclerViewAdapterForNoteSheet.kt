package com.example.mynotesheet

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.ScrollView
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.example.common.ContextHolder
import com.example.model.DataManager
import com.example.model.data.Sheet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --> 리사이클러뷰의 재활용성 때문에, 화면에 표시하는 뷰를 다 갖고 있으려고 하면 안된다.
class RecyclerViewAdapterForNoteSheet(
    private val context: Context,
    private val sheetList: MutableList<Sheet>
) : RecyclerView.Adapter<RecyclerViewAdapterForNoteSheet.ViewHolder>() {
    val mContext = context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.sheet, parent, false))
    } // 에디트 텍스트 까지 재활용을 하고.
    // 에디트 텍스트를 onBindView할때 매번 새로 만들어 넣어줘야.. 탈이 없을 것임.
    @SuppressLint("LongLogTag")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (sheetList[position] != null) {
            holder.setData(position, sheetList[position])
        }
    }

    override fun getItemCount(): Int = sheetList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("UseCompatLoadingForDrawables")
        fun setData(pos:Int, sheetInfo: Sheet) {
            Log.i("kongyi0607", "position = $pos sheetInfo.name = ${sheetInfo.getName()} sheetInfo.content = ${sheetInfo.getContent()}")
            val position = pos
            itemView.findViewById<ScrollView>(R.id.scrollView).removeAllViews()
            val view = sheetInfo.getEditTextView()!!
            if (view.parent != null) {
                ((view.parent) as ViewGroup).removeView(view)
            }
            itemView.findViewById<ScrollView>(R.id.scrollView).addView(view)

            // ※ 리사이클러 뷰에서 헤매기 쉬운 부분
            // 바뀌는 것은 모조리 데이터로 갖고 있어야 한다.
            // textView가 만약 바뀐다면, textView도 데이터로 갖고 있는게 안전하긴 하다.
            // editText같은 경우
            sheetInfo.getEditTextView()?.run {
                setText(sheetInfo.getContent()) // 여기서 셋을 하면, 기 등록된 doOnTextChanged에 의해 이전 값으로 값이 써진다.
                textSize = sheetInfo.getTextSize()
                doOnTextChanged { text, start, before, count ->
                    CoroutineScope(Dispatchers.Default).launch {
                        Log.i("kongyi0607", "input changing...")
                        Log.i("kongyi0607", "position = $position, text = ${text.toString()}")
                        sheetList[position].setContent(text.toString())
                        //  printAll()
                        //sheetInfo.setTextSize(textSize)
                        update(text.toString(), pos, sheetInfo)
                    }
                }
            }
        }
    }

    private fun printAll() {
        Log.i("kongyi0607", "-------------------------\n")
        for (sheet in sheetList) {
            Log.i("kongyi0607", "id = ${sheet.getId()} name = ${sheet.getName()} content = ${sheet.getContent()}")
        }
    }
    // debounce logic.
    @Synchronized private suspend fun update(text:String, pos:Int, sheetInfo:Sheet) {
        if (ContextHolder.lastJobForRecyclerView != null) {
            Log.i("kongyi0605", "lastJobForRecyclerView Canceled")
            ContextHolder.lastJobForRecyclerView!!.cancel()
        }

        ContextHolder.lastJobForRecyclerView = CoroutineScope(Dispatchers.Default).launch {
            delay(500)
            Log.i("kongyi0605", "lastJobForRecyclerView update completed.")
            sheetInfo.setContent(text.toString())
            saveSingleSheetIntoDB(pos, sheetInfo)
            ContextHolder.lastJobForRecyclerView = null
        }
    }

    private fun saveSingleSheetIntoDB(position: Int, sheetInfo: Sheet) {
        Log.i("kongyi0605", "position = ${position}")
        Log.i("kongyi0605", "items = ${sheetList.toString()}")
        if (sheetList != null && sheetList.size > 0) {
            sheetList[position] = sheetInfo
            DataManager.setSingleSheetOnRTDB(mContext, position, sheetInfo, -1, -1)
        }
    }
}

