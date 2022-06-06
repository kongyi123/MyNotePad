package com.example.mynotesheet

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.*
import android.widget.EditText
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.example.common.ContextHolder
import com.example.common.data.Schedule
import com.example.model.DataManager
import com.example.model.data.Sheet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

// --> 리사이클러뷰의 재활용성 때문에, 화면에 표시하는 뷰를 다 갖고 있으려고 하면 안된다.
class RecyclerViewAdapterForNoteSheet(
    private val context: Context,
    private val sheetList: MutableList<Sheet>
) : RecyclerView.Adapter<RecyclerViewAdapterForNoteSheet.ViewHolder>() {
    val mContext = context
    private var items = sheetList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.sheet, parent, false))
    }

    @SuppressLint("LongLogTag")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (items!![position] != null) {
            holder.setData(position, items[position])
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("UseCompatLoadingForDrawables")
        fun setData(pos:Int, sheetInfo: Sheet) {
            itemView.findViewById<EditText>(R.id.editText).run {
                setText(sheetInfo.getContent())
                textSize = sheetInfo.getTextSize().toFloat()

                doOnTextChanged { text, start, before, count ->
                    CoroutineScope(Dispatchers.Default).launch {
                        Log.i("kongyi0603", "input changing...")
                        update(text.toString(), pos, sheetInfo)
                    }
                }
            }

        }
    }

    // debounce logic.
    private suspend fun update(text:String, pos:Int, sheetInfo:Sheet) {
        Mutex().withLock {
            if (ContextHolder.lastJobForRecyclerView != null) {
                Log.i("kongyi0605", "lastJobForRecyclerView Canceled")
                ContextHolder.lastJobForRecyclerView!!.cancel()
            }
        }
        ContextHolder.lastJobForRecyclerView = CoroutineScope(Dispatchers.Default).launch {
            delay(2000)
            Log.i("kongyi0605", "lastJobForRecyclerView update completed.")
            sheetInfo.setContent(text.toString())
            saveSingleSheetIntoDB(pos, sheetInfo)
            ContextHolder.lastJobForRecyclerView = null
        }
    }

    private fun saveSingleSheetIntoDB(position: Int, sheetInfo: Sheet) {
        Log.i("kongyi0605", "position = ${position}")
        Log.i("kongyi0605", "items = ${items.toString()}")
        items[position] = sheetInfo
        DataManager.setSingleSheetOnRTDB(mContext, position, sheetInfo, -1, -1)
    }
}

