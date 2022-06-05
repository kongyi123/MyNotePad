package com.example.model.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.common.utility.SoftKeyboard
import com.example.model.DataManager
import com.example.model.R
import com.example.model.data.Sheet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SheetFragment(private val softKeyboard: SoftKeyboard, private val mContext: Context, private val nextSheetIdCount:Int) : Fragment() {
    // Store instance variables
    private val TAG = "SheetFragment/kongyi123"
    var content: String? = null
    var textSize: Float? = null
    var editText:EditText? = null
    var idxOfSheetsArray: Int? = null

    // Inflate the view for the fragment based on layout XML
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Log.d(TAG, "onCreateView")
        val view: View = inflater.inflate(R.layout.sheet_fragment, container, false)
        editText = view.findViewById(R.id.editText)
        softKeyboard.addEditText(editText)
        editText?.setText("$content")
        if (textSize != null) editText?.textSize = textSize!!
        Log.i("kongyi0603", "onCreateView")
        editText?.doOnTextChanged { text, start, before, count ->
            Log.i("kongyi0603", "input changing...")
            if (idxOfSheetsArray != null) {
                DataManager.sheetList.value!![idxOfSheetsArray!!].setContent(text.toString())
            }
            saveAllIntoDB()
        }
        return view
    }

    fun initialize(content:String, textSize:Float, idxOfSheets:Int) {
        Log.d(TAG, "initialize")
        this.content = content
        this.textSize = textSize
        this.idxOfSheetsArray = idxOfSheets
    }

    private fun saveAllIntoDB() {
        Log.i("kongyi0603", "saveAllIntoDB")
        CoroutineScope(Dispatchers.IO).launch {
            // 현재 프레그 먼트 덩어리에 있는 것을 저장하여 올림
            DataManager.clearSheetListFirebaseDatabase("sheet_list")
            updateFragmentToSheets()
            val nextSheetSize = DataManager.sheetList.value!!.size + 1
            for (i in 1..DataManager.sheetList.value!!.size) {
                Log.i("kongyi0603", "data = ${DataManager.sheetList.value!![i-1].getId()} ${DataManager.sheetList.value!![i-1].getName()} ${DataManager.sheetList.value!![i-1].getContent()}")
                val bringTypeSheet = Sheet(
                    DataManager.sheetList.value!![i-1].getId(),
                    DataManager.sheetList.value!![i-1].getName(),
                    DataManager.sheetList.value!![i-1].getContent(),
                    DataManager.sheetList.value!![i-1].getTextSize())
                DataManager.setSingleSheetOnRTDB(mContext, i-1, bringTypeSheet, nextSheetSize, nextSheetIdCount)
            }
            DataManager.setSheetCountOnRTDB(mContext, DataManager.sheetList.value!!.size)
            DataManager.setIdCountOnRTDB(mContext, nextSheetIdCount)
        }
    }

    private fun updateFragmentToSheets() {
        for (i in 1..DataManager.sheetList.value!!.size) {
            if (DataManager.sheetList.value!![i-1].getSheetFragment() != null) {
                val content:String = DataManager.sheetList.value!![i-1].getSheetFragment()?.editText?.text.toString()
                if (content == "null") {
                    Log.d(TAG, "content is null!!")
                } else {
                    DataManager.sheetList.value!![i - 1].setContent(content)
                }
                DataManager.sheetList.value!![i-1].setTextSize(DataManager.sheetList.value!![i-1].getTextSize()!!)
            }
        }
    }
}