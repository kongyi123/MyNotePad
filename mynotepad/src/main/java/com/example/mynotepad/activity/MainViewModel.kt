package com.example.mynotepad.activity

import android.app.Application
import android.app.Service
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.AndroidViewModel
import com.example.mynotepad.R
import com.example.mynotepad.data.Sheet
import com.example.mynotepad.utility.SoftKeyboard
import com.example.mynotepad.view.TabTextView
import androidx.viewpager2.widget.ViewPager2
import com.example.model.DataManager


class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "kongyi123/MainViewModel"
    var currentTabTitleView: TextView? = null
    var isFirstStart = true

    var sheetSize: Int = 0
    var sheetIdCount:Int = 0
    var sheetSelectionTab: LinearLayout? = null
    val sheetOrder: MutableMap<Int, Int> = mutableMapOf<Int, Int>()
    var vpPager:ViewPager2? = null
    var currentTabPosition:Int = 0
    var softKeyboard: SoftKeyboard? = null
    var rootLayout: ViewGroup? = null
    var controlManager: InputMethodManager? = null


    var items: MutableList<Sheet> = mutableListOf()
    val size: Int get() = items.size
    fun itemId(position: Int): Long = items[position].getId()!!.toLong()
    fun contains(itemId: Long): Boolean = items.any {
        it.getId()!!.toLong() == itemId
    }

//    fun addNewAt(position: Int) = items.add(position, Sheet())
    fun removeAt(position: Int) = items.removeAt(position)
    fun createIdSnapshot(): List<Long> = (0 until size).map { position -> itemId(position) }


    /** Initialize
     */
    fun initialize(context:Context, supportFragmentManager:FragmentManager):Boolean {
        sheetSelectionTab = (context as AppCompatActivity).findViewById(R.id.tabInner)
        loadSheetData()
        rootLayout = context.findViewById<LinearLayout>(R.id.root_layout)
        controlManager = context.getSystemService(Service.INPUT_METHOD_SERVICE) as InputMethodManager
        softKeyboard = SoftKeyboard(rootLayout, controlManager, context)
        softKeyboard!!.setSoftKeyboardCallback(object : SoftKeyboard.SoftKeyboardChanged {
            override fun onSoftKeyboardHide() {
                Log.d(TAG, "keyboard hided")
            }

            override fun onSoftKeyboardShow() {
                Log.d(TAG, "keyboard onSoftKeyboardShow")
            }
        })
//        initViewPager(vpPager!!, supportFragmentManager)
        return true
    }

    fun updateFragmentToSheets() {
        for (i in 1..items.size) {
            if (items[i-1].getSheetFragment() != null) {
                val content:String? = items[i-1].getSheetFragment()?.editText?.text.toString()
                if (content == "null") {
                    Log.d(TAG, "content is null!!")
                } else {
                    items[i - 1].setContent(content)
                }
                items[i-1].setTextSize(items[i-1].getTextSize()!!)
            }
        }
    }

    /** Switch focused Sheet in Tab at the bottom line at the screen
     */
    fun switchFocusSheetInTab(position:Int) {
        switchFocusSheetInTab(items?.get(position)?.getTabTitleView() as View)
    }

    /** Save all of the data in the application.
     */
    fun saveAllIntoDB() {
        val context:Context = getApplication()

        // 현재 프레그 먼트 덩어리에 있는 것을 저장하여 올림
        updateFragmentToSheets()
        for (i in 1..items.size) {
            val bringTypeSheet = com.example.model.data.Sheet(
                items[i-1].getId(),
                items[i-1].getName(),
                items[i-1].getContent(),
                items[i-1].getTextSize())
            DataManager.setSingleSheet(context, i-1, bringTypeSheet)
        }
        DataManager.setSheetCount(context, items.size)
        DataManager.setIdCount(context, sheetIdCount)
        Toast.makeText(getApplication(), "saved", Toast.LENGTH_SHORT).show()
    }

    /** Load All of the Sheet data
     */
    private fun loadSheetData() {
        val context:Context = getApplication()
        isFirstStart = false
        sheetSize = DataManager.getSheetCount(context)
        sheetIdCount = DataManager.getIdCount(context)
        if (sheetSize > 0) {
            for (i in 1..sheetSize) {
                val item:com.example.model.data.Sheet = DataManager.getSingleSheet(context, i)
                var sheetName = item.getName()
                var sheetContent = item.getContent()
                var sheetId:String? = item.getId().toString()
                var sheetTextSize:Float? = item.getTextSize()
                if (sheetTextSize == -1.0f) {
                    sheetTextSize = 10.0f
                }
                if (sheetId != null) {
                    val textView = TabTextView(context.applicationContext);
                    items?.add(Sheet(sheetId!!.toInt(), sheetName, sheetContent, textView, sheetTextSize))
                    val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    textView.layoutParams = params
                    textView.text = sheetName
                    textView.id = sheetId!!.toInt()
                    textView.setBackgroundColor(context.resources.getColor(R.color.colorDeactivatedSheet))
                    sheetOrder?.set(textView.id, i - 1)
                    textView.setOnClickListener {
                        switchFocusSheetInTab(it)
                        sheetOrder?.get((it as TextView).id)?.let { it1 ->
//                                Toast.makeText(this, "it1 = " + it1, Toast.LENGTH_SHORT).show()
                            vpPager?.setCurrentItem(it1, true)
                            currentTabPosition = it1
                        }
                    }
                    addShowingSheetInTab(textView)
                }
            }
            currentTabTitleView = items?.get(0)?.getTabTitleView()
            if (currentTabTitleView != null) {
                currentTabTitleView!!.setBackgroundColor(context.resources.getColor(
                    R.color.colorActivatedSheet
                ))
            }
        } else {
            Toast.makeText(context, "저장된 데이터가 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    /** Switch focus sheet in bottom tab
     * - change currentSheetId, currentTabTextView
     */
    private fun switchFocusSheetInTab(it:View) {
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



    /** Add new title view of a sheet into the tab bottom side of screen
     * - If there is previous parent (linear layout) of view, it should be called removeView method.
     */
    private fun addShowingSheetInTab(view: View) {
        if (view.parent != null) {
            ((view.parent) as ViewGroup).removeView(view)
        }
        sheetSelectionTab?.addView(view)
    }

    fun removeShowingSheetInTab(view: View) {
        if (view.parent != null) {
            ((view.parent) as ViewGroup).removeView(view)
        }
    }

    /** Increase text size of the text content in current text screen
     */
    fun contentTextSizeIncrease() {
        val currentContentTextSize = items[currentTabPosition].getTextSize()!! + 1
        items[currentTabPosition].setTextSize(currentContentTextSize)
    }

    /** Decrease text size of the text content in current text screen
     */
    fun contentTextSizeDecrease() {
        val currentContentTextSize = items[currentTabPosition].getTextSize()!! - 1
        items[currentTabPosition].setTextSize(currentContentTextSize)
    }

//    fun addNewSheet(context: Context, vpPager: ViewPager, switchFocusSheetInTab: (View) -> Unit, addShowingSheet: (TextView) -> Unit) {
    fun addNewSheet(vpPager: ViewPager2) {
        val context:Context = getApplication()
            val textView = TabTextView(context)
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        textView.layoutParams = params
        textView.text = "newSheet"
        textView.id = ++sheetIdCount
//        textView.background = getDrawable(R.drawable.edge)
        textView.setBackgroundColor(context.resources.getColor(R.color.colorActivatedSheet))
//        textView.typeface = resources.getFont(R.font.whj000f0cb5)
        Log.d(TAG, "view textView = " + textView)
        sheetSize++
        sheetOrder?.set(textView.id, sheetSize-1)

        textView?.setOnClickListener {
            switchFocusSheetInTab(it)
            vpPager.setCurrentItem(sheetOrder[it.id]!!, true)
            Log.d(TAG, "pos = " + sheetOrder[it.id]!!)
        }
        items?.add(Sheet(sheetIdCount!!, "newSheet", "new", textView, 10.0f))

        addShowingSheetInTab(textView)

//        adapterViewPager?.getItem()
        vpPager.adapter!!.notifyDataSetChanged()
        switchFocusSheetInTab(textView)
        vpPager.setCurrentItem(sheetSize-1, true)
    }

    override fun onCleared() {
        super.onCleared()
        softKeyboard?.unRegisterSoftKeyboardCallback();
    }
}