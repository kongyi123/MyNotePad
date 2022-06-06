package com.example.mynotesheet

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.viewpager2.widget.ViewPager2
import com.example.model.DataManager
import com.example.model.data.Sheet
import java.util.concurrent.atomic.AtomicBoolean

/*
    3. 삭제 기능
    4. 시트명 편집 기능
 */

class NoteSheetActivity : AppCompatActivity() {
    private val TAG = "NoteSheetActivity"
    private lateinit var mMemoPager: ViewPager2
    private lateinit var mMemoAdapter: RecyclerViewAdapterForNoteSheet
    private var sheetSelectionTab: LinearLayout? = null
    private lateinit var tabOuter:LinearLayout
    var currentTabPosition:Int = 0
    var sheetLastId:Int = 0
    private val isTabClicked = AtomicBoolean(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_sheet)
        mMemoPager = findViewById(R.id.vpPager)
        mMemoAdapter = RecyclerViewAdapterForNoteSheet(this, DataManager.sheetList.value ?: ArrayList<Sheet>())
        sheetLastId = getLastSheetId(DataManager.sheetList.value ?: ArrayList<Sheet>())
        mMemoPager.adapter = mMemoAdapter
        mMemoPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        val onPageChangeCallbackForCalendar = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                DataManager.sheetList.value?.get(position)?.let {
                    if (isTabClicked.get()) {
                        isTabClicked.set(false)
                    } else {
                        switchFocusSheetInTab(it.getTabTitleView()!!)
                    }
                }
            }
        }
        sheetSelectionTab = findViewById(R.id.tabInner)
        tabOuter = findViewById(R.id.tabOuter)

        mMemoPager.registerOnPageChangeCallback(onPageChangeCallbackForCalendar)
        mMemoPager.setCurrentItem(0, false)


        DataManager.sheetList.observe(this, androidx.lifecycle.Observer {
            initialTab()
        })
    }

    private fun getLastSheetId(sheetList: MutableList<Sheet>):Int {
        var lastSheetId = 0
        for (sheetInfo in sheetList) {
            if (lastSheetId < sheetInfo.getId()!!.toInt()) {
                lastSheetId = sheetInfo.getId()!!.toInt()
            }
        }
        return lastSheetId
    }

    private fun initialTab() {
        Log.i("kongyi0606", "initialTab()")
        val sheetSize = DataManager.sheetList.value!!.size
        sheetSelectionTab?.removeAllViews()
        for (i in 0 until sheetSize) {
            val textView = DataManager.sheetList.value!![i].getTabTitleView()
            Log.i("kongyi0606_", "textview.id = ${textView?.id}")
            textView?.let {
                textView.setOnClickListener {
                    Log.i("kongyi0606", "currentTabPosition = ${currentTabPosition}")
                    isTabClicked.set(true)
                    switchFocusSheetInTab(it)
                    mMemoPager.setCurrentItem(it.id, true)
                }
                textView.setBackgroundColor(resources.getColor(R.color.colorDeactivatedSheet))

                if (textView != null) {
                    Log.i("kongyi0606", "addShowingSheetInTab will be called soon")
                    addShowingSheetInTab(textView)
                }
            }
        }
        if (sheetSize > 0) {
            val currentTabTitleView = DataManager.sheetList.value!![currentTabPosition].getTabTitleView()
            Log.i("kongyi0606", "currentTabTitleView = " + currentTabTitleView)
            currentTabTitleView?.setBackgroundColor(
                resources.getColor(
                    R.color.colorActivatedSheet
                )
            )
        }
    }


    /** Add new title view of a sheet into the tab bottom side of screen
     * - If there is previous parent (linear layout) of view, it should be called removeView method.
     */
    private fun addShowingSheetInTab(view: View) {
        Log.i("kongyi0420", "addShowingSheetInTab")
        if (view.parent != null) {
            ((view.parent) as ViewGroup).removeView(view)
        }
        sheetSelectionTab?.addView(view)
    }


    private fun deleteCurrentSheet() {
        if (DataManager.sheetList.value!!.size <= 0) return

    }


    private fun switchFocusSheetInTab(it:View) {
        Log.d(TAG, "switchFocusSheetInTab")
        Log.i("kongyi0606_", "from = $currentTabPosition, to = ${it.id}")
        Log.i("kongyi0606_", "sheetList = ${DataManager.sheetList.value.toString()}")
        val targetTextView = it as TextView
        val currentTabTitleView = DataManager.sheetList.value!![currentTabPosition].getTabTitleView()

        if (targetTextView.id == currentTabTitleView?.id) return

        val sourceTextView = currentTabTitleView
        Log.i("kongyi0606", "sourceTextView = $sourceTextView")
        sourceTextView?.setBackgroundColor(resources.getColor(R.color.colorDeactivatedSheet))
        targetTextView.setBackgroundColor(resources.getColor(R.color.colorActivatedSheet))

        // StateVariable update
        currentTabPosition = targetTextView.id
        tabOuter.requestFocus()
    }

    fun onClickPlusIcon(view: View) {

    }

    /** Increase text size of the text content in current text screen
     */
    private fun contentTextSizeIncrease() {
        val currentContentTextSize = DataManager.sheetList.value!![currentTabPosition].getTextSize()!! + 1
        DataManager.sheetList.value!![currentTabPosition].setTextSize(currentContentTextSize)
        Log.i("kongyi0606", "currentContentTextSize = $currentContentTextSize")
        mMemoAdapter.notifyDataSetChanged()
    }

    /** Decrease text size of the text content in current text screen
     */
    private fun contentTextSizeDecrease() {
        val currentContentTextSize = DataManager.sheetList.value!![currentTabPosition].getTextSize()!! - 1
        DataManager.sheetList.value!![currentTabPosition].setTextSize(currentContentTextSize)
        Log.i("kongyi0606", "currentContentTextSize = $currentContentTextSize")
        mMemoAdapter.notifyDataSetChanged()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.memo_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menuEditSheetNameBtn-> makeDialogAndEditSheetName()
            R.id.menuDeleteSheetBtn-> deleteCurrentSheet()
            R.id.menuTextSizeIncreaseBtn-> contentTextSizeIncrease()
            R.id.menuTextSizeDecreaseBtn-> contentTextSizeDecrease()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun makeDialogAndEditSheetName() {
        // make Dialog
        val dlg: AlertDialog.Builder = AlertDialog.Builder(this)
        val ad: AlertDialog = dlg.create()
        ad.setTitle("Edit Name") //제목
        val inflater: LayoutInflater = LayoutInflater.from(applicationContext)
        val view = inflater.inflate(R.layout.dialog, findViewById(R.id.root_layout), false)
        ad.setView(view) // 메시지
        // set New Sheet Name if the confirm button is clicked.
        view.findViewById<Button>(R.id.dialogConfirmBtn).setOnClickListener {

        }
        ad.show()
    }

}