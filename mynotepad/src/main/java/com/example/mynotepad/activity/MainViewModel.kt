package com.example.mynotepad.activity

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.AndroidViewModel
import com.example.model.data.Sheet
import androidx.viewpager2.widget.ViewPager2
import com.example.model.DataManager
import com.example.model.view.TabTextView
import com.example.mynotepad.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean


class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "kongyi123/MainViewModel"
    @SuppressLint("StaticFieldLeak")
    var currentTabTitleView: TextView? = null
    var isFirstStart = true

    var sheetSize: Int = 0
    var sheetIdCount:Int = 0
    val sheetOrder: MutableMap<Int, Int> = mutableMapOf<Int, Int>()
    @SuppressLint("StaticFieldLeak")
    var currentTabPosition:Int = 0

    val size: Int get() = DataManager.sheetList.value!!.size
    @SuppressLint("StaticFieldLeak")

    fun itemId(position: Int): Long = DataManager.sheetList.value!![position].getId()!!.toLong()
    fun contains(itemId: Long): Boolean = DataManager.sheetList.value!!.any {
        it.getId()!!.toLong() == itemId
    }

//    fun addNewAt(position: Int) = items.add(position, Sheet())
    fun removeAt(position: Int) = DataManager.sheetList.value!!.removeAt(position)
    fun createIdSnapshot(): List<Long> = (0 until size).map { position -> itemId(position) }

    fun updateFragmentToSheets() {
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

    /** Switch focused Sheet in Tab at the bottom line at the screen
     */
    fun switchFocusSheetInTab(position:Int) {
        switchFocusSheetInTab(DataManager.sheetList.value!![position].getTabTitleView() as View)
    }

    /** Save all of the data in the application.
     */
    fun saveAllIntoDB() {
        val context:Context = getApplication()

        // 현재 프레그 먼트 덩어리에 있는 것을 저장하여 올림
        updateFragmentToSheets()
        for (i in 1..DataManager.sheetList.value!!.size) {
            val bringTypeSheet = Sheet(
                DataManager.sheetList.value!![i-1].getId(),
                DataManager.sheetList.value!![i-1].getName(),
                DataManager.sheetList.value!![i-1].getContent(),
                DataManager.sheetList.value!![i-1].getTextSize())
            DataManager.setSingleSheetOnRTDB(context, i-1, bringTypeSheet)
        }
        DataManager.setSheetCountOnRTDB(context, DataManager.sheetList.value!!.size)
        DataManager.setIdCountOnRTDB(context, sheetIdCount)
        Toast.makeText(getApplication(), "saved", Toast.LENGTH_SHORT).show()
    }

    /** Load All of the Sheet data
     */
    fun loadSheetData() {
        Log.i("kongyi0421", "loadSheetData")

        val context:Context = getApplication()
        isFirstStart = false
        sheetSize = DataManager.sheetList.value!!.size
        CoroutineScope(Dispatchers.Main).launch {
            sheetIdCount = DataManager.getIdCountFromRTDB(context)
        }

        Log.i("kongyi0420", "isRead is true")
    }

    /** Switch focus sheet in bottom tab
     * - change currentSheetId, currentTabTextView
     */
    fun switchFocusSheetInTab(it:View) {
        val context:Context = getApplication()
        Log.d(TAG, "switchFocusSheetInTab")
        val targetTextView = it as TextView
        if (targetTextView.id == currentTabTitleView?.id) return

        val sourceTextView = currentTabTitleView
        sourceTextView?.setBackgroundColor(context.resources.getColor(R.color.colorDeactivatedSheet))
        targetTextView.setBackgroundColor(context.resources.getColor(R.color.colorActivatedSheet))

        // StateVariable update
        currentTabTitleView = targetTextView
        Log.d(TAG, "switchFocusSheetInTab : currentTabTitleView(targetTextView) = " + targetTextView.text)
    }

    fun removeShowingSheetInTab(view: View) {
        if (view.parent != null) {
            ((view.parent) as ViewGroup).removeView(view)
        }
    }

    /** Increase text size of the text content in current text screen
     */
    fun contentTextSizeIncrease() {
        val currentContentTextSize = DataManager.sheetList.value!![currentTabPosition].getTextSize()!! + 1
        DataManager.sheetList.value!![currentTabPosition].setTextSize(currentContentTextSize)
    }

    /** Decrease text size of the text content in current text screen
     */
    fun contentTextSizeDecrease() {
        val currentContentTextSize = DataManager.sheetList.value!![currentTabPosition].getTextSize()!! - 1
        DataManager.sheetList.value!![currentTabPosition].setTextSize(currentContentTextSize)
    }

//    fun addNewSheet(context: Context, vpPager: ViewPager, switchFocusSheetInTab: (View) -> Unit, addShowingSheet: (TextView) -> Unit) {
@SuppressLint("NotifyDataSetChanged")
fun addNewSheet(vpPager: ViewPager2, textView:TextView) {
        DataManager.sheetList.value!!.add(Sheet(sheetIdCount, "newSheet", "new", textView, 10.0f))
//        adapterViewPager?.getItem()
        vpPager.adapter!!.notifyDataSetChanged()
        switchFocusSheetInTab(textView)
        vpPager.setCurrentItem(sheetSize-1, true)
    }
}