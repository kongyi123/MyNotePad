package com.example.mynotesheet

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import androidx.viewpager2.widget.ViewPager2
import com.example.model.DataManager
import com.example.model.data.Sheet
import com.example.model.view.TabTextView
import java.util.concurrent.atomic.AtomicBoolean

/*
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
    private var isFirst = true
    private val sheetList = ArrayList<Sheet>()
    private val sheetOrder: MutableMap<Int, Int> = mutableMapOf<Int, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_sheet)
        DataManager.sheetList.observe(this, androidx.lifecycle.Observer {
            if (isFirst) {
                isFirst = false
                initialization()
            }
        })
    }

    private fun initialization() {
        Log.i("kongyi0608", "initialization")
        for (sheet in DataManager.sheetList.value!!) {
            sheetList.add(Sheet(sheet.getId()!!, sheet.getName(), sheet.getContent(), sheet.getTabTitleView(), sheet.getTextSize(), sheet.getEditTextView()))
        }

        mMemoPager = findViewById(R.id.vpPager)
        mMemoAdapter =
            RecyclerViewAdapterForNoteSheet(this, sheetList)
        sheetLastId = this.getLastSheetIdWithRefreshSheetInfo(sheetList)
        mMemoPager.adapter = mMemoAdapter
        mMemoPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        val onPageChangeCallbackForCalendar = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.i("kongyi0609", "onPageChanged position : $position")
                sheetList[position].let {
                    if (isTabClicked.get()) {
                        isTabClicked.set(false)
                    } else {
                        switchFocusSheetInTab(position, false)
                    }
                }
            }
        }
        sheetSelectionTab = findViewById(R.id.tabInner)
        tabOuter = findViewById(R.id.tabOuter)

        mMemoPager.registerOnPageChangeCallback(onPageChangeCallbackForCalendar)
        mMemoPager.setCurrentItem(0, false)
        initialTab()
    }

    private fun getLastSheetIdWithRefreshSheetInfo(sheetList: MutableList<Sheet>):Int {
        var lastSheetId = 0
        var cnt = 0
        sheetOrder.clear()
        for (sheetInfo in sheetList) {
            sheetOrder[sheetInfo.getTabTitleView()!!.id] = cnt++
            if (lastSheetId < sheetInfo.getId()!!.toInt()) {
                lastSheetId = sheetInfo.getId()!!.toInt()
            }
        }
        return lastSheetId
    }

    private fun initialTab() {
        Log.i("kongyi0606", "initialTab()")
        val sheetSize = sheetList.size
        sheetSelectionTab?.removeAllViews()
        for (i in 0 until sheetSize) {
            val textView = sheetList[i].getTabTitleView()
            Log.i("kongyi0608", "textview id = ${sheetOrder[textView?.id]}")
            textView?.let {
                textView.setOnClickListener {
                    Log.i("kongyi0606", "currentTabPosition = ${currentTabPosition}")
                    isTabClicked.set(true)
                    switchFocusSheetInTab(sheetOrder[it.id]!!, false)
                    mMemoPager.setCurrentItem(sheetOrder[it.id]!!, true)
                }
                textView.setBackgroundColor(resources.getColor(R.color.colorDeactivatedSheet))

                if (textView != null) {
                    Log.i("kongyi0606", "addShowingSheetInTab will be called soon")
                    addShowingSheetInTab(textView)
                }
            }
        }
        if (sheetSize > 0) {
            val currentTabTitleView = sheetList[currentTabPosition].getTabTitleView()
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

    private fun itemId(position: Int): Long = sheetList[position].getId()!!.toLong()
    fun createIdSnapshot(): List<Long> = (0 until sheetList.size).map { position -> itemId(position) }

    private fun deleteCurrentSheet() {
        val lastPosOfOriginalList = sheetList.size-1
        if (sheetList.size <= 0) return
        val target = currentTabPosition

        // tab
        Log.i("kongyi0608", "currentTabPosition!!!!!!!!!!!!!! = ${currentTabPosition}")
        val currentTabTitleView = sheetList[currentTabPosition].getTabTitleView()
        removeShowingSheetInTab(currentTabTitleView as View)

        // remove current sheet in DB
        DataManager.removeSingleSheet("sheet_list", sheetList[currentTabPosition].getId().toString())

        // sheet
        val idsOld = createIdSnapshot()
        sheetList.removeAt(target)
        sheetLastId = this.getLastSheetIdWithRefreshSheetInfo(sheetList)
        val idsNew = createIdSnapshot()
        DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = idsOld.size
            override fun getNewListSize(): Int = idsNew.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                idsOld[oldItemPosition] == idsNew[newItemPosition]
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                areItemsTheSame(oldItemPosition, newItemPosition)
        }, true).dispatchUpdatesTo(mMemoPager.adapter!!)
        mMemoAdapter.notifyDataSetChanged()

        if (lastPosOfOriginalList == target) {
            Log.i("kongyi0609", "removed last one")
            switchFocusSheetInTab(sheetList.size-1, true)
            mMemoPager.setCurrentItem(sheetList.size-1, true)
        } else {
            switchFocusSheetInTab(target, true)
        }
    }

    private fun removeShowingSheetInTab(view: View) {
        if (view.parent != null) {
            ((view.parent) as ViewGroup).removeView(view)
        }
    }

    private fun switchFocusSheetInTab(target: Int, isFromDelete:Boolean) {
        Log.d(TAG, "switchFocusSheetInTab")
        Log.i("kongyi0609", "from = $currentTabPosition to = $target")
        val targetTextView = sheetList[target].getTabTitleView() as TextView

        if (!isFromDelete) {
            val currentTabTitleView = sheetList[currentTabPosition].getTabTitleView()

            if (targetTextView.id == currentTabTitleView?.id) {
                Log.i("kongyi0608", "targetTextView.id = ${targetTextView.id}")
                Log.i("kongyi0608", "currentTabTitleView.id = ${currentTabTitleView.id}")
                return
            }

            currentTabTitleView?.setBackgroundColor(resources.getColor(R.color.colorDeactivatedSheet))
        }

        targetTextView.setBackgroundColor(resources.getColor(R.color.colorActivatedSheet))

        // StateVariable update
        currentTabPosition = target
        tabOuter.requestFocus()
    }

    fun onClickPlusIcon(view: View) {
        // 액티비디 단에서 만들어서 보여주고
        // 별도로 모델 단에 추가해줄 것.
        val newSheetId = ++sheetLastId
        val context: Context = application
        val textView = TabTextView(context.applicationContext)
        val sheetId = newSheetId
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        textView.layoutParams = params
        textView.text = "newSheet"
        textView.id = sheetId // 동적 생성시 default는 무조건 0이 됨.
        textView.setBackgroundColor(context.resources.getColor(R.color.colorDeactivatedSheet))
        val editTextView = EditText(context.applicationContext)
        val params2 = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        editTextView.layoutParams = params2
        editTextView.setText("new")
        editTextView.setPadding(20, 10, 20, 10)
        editTextView.setEms(10)
        editTextView.gravity = Gravity.TOP
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            editTextView.textCursorDrawable = context.getDrawable(R.drawable.text_cursor)
        }
        editTextView.setBackgroundResource(0)
        //editTextView.setTypeface(null, Typeface.NORMAL);
        editTextView.canScrollHorizontally(- 1)
        editTextView.isNestedScrollingEnabled = false
        editTextView.setLineSpacing(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5.0f,  context.resources.displayMetrics), 1.0f);
        val newSheet = Sheet(newSheetId, "newSheet", "new", textView, 10.0f, editTextView)
        sheetList.add(newSheet)
        mMemoPager.adapter!!.notifyDataSetChanged()
        val newSheetPos = sheetList.size-1
        mMemoPager.setCurrentItem(newSheetPos, true)
        addShowingSheetInTab(textView)
        sheetOrder[textView.id] = newSheetPos
        textView.setOnClickListener {
            isTabClicked.set(true)
            switchFocusSheetInTab(sheetOrder[textView.id]!!, false)
            mMemoPager.setCurrentItem(sheetOrder[textView.id]!!, true)
        }
        switchFocusSheetInTab(sheetOrder[textView.id]!!, false)

        // DB에 추가
        DataManager.setSingleSheetOnRTDB(this, -1, newSheet, newSheetId)
    }


    /** Increase text size of the text content in current text screen
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun contentTextSizeIncrease() {
        val currentContentTextSize = sheetList[currentTabPosition].getTextSize()!! + 1
        sheetList[currentTabPosition].setTextSize(currentContentTextSize)
        Log.i("kongyi0606", "currentContentTextSize = $currentContentTextSize")
        mMemoAdapter.notifyDataSetChanged()
    }

    /** Decrease text size of the text content in current text screen
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun contentTextSizeDecrease() {
        val currentContentTextSize = sheetList[currentTabPosition].getTextSize()!! - 1
        sheetList[currentTabPosition].setTextSize(currentContentTextSize)
        Log.i("kongyi0606", "currentContentTextSize = $currentContentTextSize")
        mMemoAdapter.notifyDataSetChanged()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.mynote_menu, menu)
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
            val newName = view.findViewById<EditText>(R.id.dialogEditBox).text.toString()
            sheetList[currentTabPosition].setName(newName)
            sheetList[currentTabPosition].getTabTitleView()?.text = newName
            val bringTypeSheet = Sheet(
                sheetList[currentTabPosition].getId(),
                sheetList[currentTabPosition].getName(),
                sheetList[currentTabPosition].getContent(),
                sheetList[currentTabPosition].getTextSize())
            DataManager.setSingleSheetOnRTDB(this, currentTabPosition, bringTypeSheet, -1)
            ad.dismiss()
        }
        ad.show()
    }
}