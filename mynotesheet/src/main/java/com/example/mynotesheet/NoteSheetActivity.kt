package com.example.mynotesheet

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.viewpager2.widget.ViewPager2
import com.example.model.DataManager
import com.example.model.data.Sheet

/*
    1. 탭 기능
    2. 시트 새로 만듦 기능
    3. 삭제 기능

 */

class NoteSheetActivity : AppCompatActivity() {
    private lateinit var mCalendarPager: ViewPager2
    private lateinit var mCalendarAdapter: RecyclerViewAdapterForNoteSheet
    private var sheetSelectionTab: LinearLayout? = null
    val sheetOrder: MutableMap<Int, Int> = mutableMapOf<Int, Int>()
    private lateinit var tabOuter:LinearLayout
    var currentTabPosition:Int = 0
    var sheetLastId:Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_sheet)
        mCalendarPager = findViewById(R.id.vpPager)
        mCalendarAdapter = RecyclerViewAdapterForNoteSheet(this, DataManager.sheetList.value ?: ArrayList<Sheet>())
        sheetLastId = getLastSheetId(DataManager.sheetList.value ?: ArrayList<Sheet>())
        mCalendarPager.adapter = mCalendarAdapter
        mCalendarPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        val onPageChangeCallbackForCalendar = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
//                mCurrentPosition = position
//                mTitlePager.setCurrentItem(position, true)
            }
        }
        sheetSelectionTab = findViewById(R.id.tabInner)
        tabOuter = findViewById(R.id.tabOuter)

        mCalendarPager.registerOnPageChangeCallback(onPageChangeCallbackForCalendar)
        mCalendarPager.setCurrentItem(0, false)
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
        Log.i("kongyi0605", "initialTab()")
       val sheetSize = DataManager.sheetList.value!!.size
        for (i in 0 until DataManager.sheetList.value!!.size) {
            Log.i("kongyi0605", "i = $i")

            val textView = DataManager.sheetList.value!![i].getTabTitleView()
            textView?.let {
                sheetOrder?.set(textView.id, i)
                textView.setOnClickListener {
                    //switchFocusSheetInTab(it)
                    sheetOrder?.get((it as TextView).id)?.let { it1 ->
                        //                                Toast.makeText(this, "it1 = " + it1, Toast.LENGTH_SHORT).show()
                        mCalendarPager.setCurrentItem(it1, true)
                    }
                }
                textView.setBackgroundColor(resources.getColor(R.color.colorDeactivatedSheet))

                if (textView != null) {
                    Log.i("kongyi0605", "addShowingSheetInTab will be called soon")
                    addShowingSheetInTab(textView)
                }
            }
        }
        if (sheetSize > 0) {
            val currentTabTitleView = DataManager.sheetList.value!![0].getTabTitleView()
            Log.i("kongyi0421", "currentTabTitleView = " + currentTabTitleView)
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

//
//    private fun deleteCurrentSheet() {
//        if (DataManager.sheetList.value!!.size <= 0) return
//        for (i in 0 until DataManager.sheetList.value!!.size) {
//            val textViewId: Int = DataManager.sheetList.value?.get(i)?.getId()!!
//            val order = sheetOrder?.get(textViewId)
//            if (order != null && order >= currentTabPosition!!) {
//                sheetOrder?.set(textViewId, order-1)
//            }
//        }
//        val target = currentTabPosition!!
//        val idsOld = modelView?.createIdSnapshot()
//        modelView?.removeAt(target)
//        val idsNew = modelView?.createIdSnapshot()
//        DiffUtil.calculateDiff(object : DiffUtil.Callback() {
//            override fun getOldListSize(): Int = idsOld!!.size
//            override fun getNewListSize(): Int = idsNew!!.size
//            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
//                idsOld!![oldItemPosition] == idsNew!![newItemPosition]
//            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
//                areItemsTheSame(oldItemPosition, newItemPosition)
//        }, true).dispatchUpdatesTo(mCalendarPager.adapter!!)
//
//        modelView?.removeShowingSheetInTab(modelView?.currentTabTitleView as View)
//        if (isTargetLastElement(target)) {
//            if (DataManager.sheetList.value!!.size > 1) {
//                switchFocusSheetInTab(target - 1)
//            }
//        } else {
//            switchFocusSheetInTab(target)
//        }
//    }
//
//    private fun switchFocusSheetInTab(position: Int) {
//        modelView?.switchFocusSheetInTab(position)
//        modelView?.updateFragmentToSheets()
//        tabOuter.requestFocus()
//        //showAllData("switchFocusSheetInTab")
//    }

    fun onClickPlusIcon(view: View) {

    }

    /** Increase text size of the text content in current text screen
     */
    private fun contentTextSizeIncrease() {
        val currentContentTextSize = DataManager.sheetList.value!![currentTabPosition].getTextSize()!! + 1
        DataManager.sheetList.value!![currentTabPosition].setTextSize(currentContentTextSize)
    }

    /** Decrease text size of the text content in current text screen
     */
    private fun contentTextSizeDecrease() {
        val currentContentTextSize = DataManager.sheetList.value!![currentTabPosition].getTextSize()!! - 1
        DataManager.sheetList.value!![currentTabPosition].setTextSize(currentContentTextSize)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.memo_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
         //   R.id.menuEditSheetNameBtn-> makeDialogAndEditSheetName()
         //   R.id.menuDeleteSheetBtn-> deleteCurrentSheet()
            R.id.menuTextSizeIncreaseBtn-> contentTextSizeIncrease()
            R.id.menuTextSizeDecreaseBtn-> contentTextSizeDecrease()
        }
        return super.onOptionsItemSelected(item)
    }

//
//    private fun makeDialogAndEditSheetName() {
//        // make Dialog
//        val dlg: AlertDialog.Builder = AlertDialog.Builder(this)
//        val ad: AlertDialog = dlg.create()
//        ad.setTitle("Edit Name") //제목
//        val inflater: LayoutInflater = LayoutInflater.from(applicationContext)
//        val view = inflater.inflate(R.layout.dialog, findViewById(R.id.root_layout), false)
//        ad.setView(view) // 메시지
//        // set New Sheet Name if the confirm button is clicked.
//        view.findViewById<Button>(R.id.dialogConfirmBtn).setOnClickListener {
//            for (i in 1..DataManager.sheetList.value!!.size) {
//                if (modelView?.currentTabTitleView?.id == DataManager.sheetList.value!!.get(i - 1)?.getId()) {
//                    DataManager.sheetList.value!!.get(i - 1)?.getTabTitleView()?.text = view.findViewById<EditText>(R.id.dialogEditBox).text
//                    DataManager.sheetList.value!!.get(i - 1)?.setName(view.findViewById<EditText>(R.id.dialogEditBox).text.toString())
//                    break
//                }
//            }
//            ad.dismiss()
//        }
//        ad.show()
//    }



    override fun onBackPressed() {
        super.onBackPressed()
        saveAllIntoDB()
    }

    private fun saveAllIntoDB() {
        Log.i("kongyi0605", "saveAllIntoDB")
        val sheetList = DataManager.sheetList.value
        val size = sheetList!!.size
        for (i in 0 until size) {
            val sheetInfo = sheetList[i]
            DataManager.setSingleSheetOnRTDB(this, i, sheetInfo, -1, -1)
        }
        DataManager.setIdCount(this, sheetLastId)
    }
}