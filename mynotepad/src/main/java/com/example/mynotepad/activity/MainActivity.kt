package com.example.mynotepad.activity

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.common.utility.SoftKeyboard
import com.example.model.DataManager
import com.example.mynotepad.R
import com.example.model.PreferenceManager
import com.example.model.view.SheetFragment
import com.example.model.view.TabTextView
import com.example.mynotepad.utility.TTSpeech
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val TAG = "kongyi123/MainActivity"
    private var modelView: MainViewModel? = null
    private var tts: TTSpeech? = null

    // option setting
    private val CLEAR = false
    private lateinit var tabOuter:LinearLayout
    private lateinit var vpPager:ViewPager2

    var controlManager: InputMethodManager? = null
    var rootLayout: ViewGroup? = null
    var softKeyboard: SoftKeyboard? = null
    var sheetSelectionTab: LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        modelView = ViewModelProvider(this)[MainViewModel::class.java]
        vpPager = findViewById(R.id.vpPager)
        tts = TTSpeech(this)

        tabOuter = findViewById(R.id.tabOuter)
        sheetSelectionTab = findViewById(R.id.tabInner)
        clearData()

        if (modelView?.isFirstStart == true) {
            modelView?.loadSheetData()
            settingSIP(this)
//            vpPager.adapter = createViewPagerAdapter()
        }

        tts?.initTTS()
        if (modelView?.sheetLastId == 0) {
            modelView?.sheetLastId = 15
        }

        DataManager.sheetList.observe(this) {
            if (modelView?.isFirstStart == true) {
                Log.i("kongyi0605", "sheetList Updated")
                modelView?.isFirstStart = false
                vpPager.registerOnPageChangeCallback(object :
                    ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        if (modelView?.size!! > 0 && position >= 0 && position < modelView?.sheetLastId!!) {
                            switchFocusSheetInTab(position)
                        }
                        modelView?.currentTabPosition = position
                    }
                })
                initialTab()
                vpPager.adapter = createViewPagerAdapter()
                initializeHotKey()

            }
        }



    }

    private fun createViewPagerAdapter(): RecyclerView.Adapter<*> {
        val items = modelView
        return object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int): SheetFragment {
                val sheetFragment = SheetFragment(softKeyboard!!, this@MainActivity, modelView!!.sheetLastId)
                sheetFragment.initialize(DataManager.sheetList.value!![position].getContent()!!, DataManager.sheetList.value!![position].getTextSize()!!, position)
                DataManager.sheetList.value!![position].setSheetFragment(sheetFragment)
                return sheetFragment
            }

            override fun getItemCount(): Int = items!!.size
            override fun getItemId(position: Int): Long = items!!.itemId(position)
            override fun containsItem(itemId: Long): Boolean = items!!.contains(itemId)
        }
    }

    private fun initialTab() {
        Log.i("kongyi0605", "initialTab()")
        if (DataManager.sheetList.value!!.size <= 0) {
            Log.i("kongyi0605", "DataManager.sheetList.value!!.size == ${DataManager.sheetList.value!!.size}")
            Log.i("kongyi0605", "modelView?.sheetSize!! == ${modelView?.sheetSize!!}")
            return
        } else {
            Log.i("kongyi0605", "items.size = ${DataManager.sheetList.value!!.size}")
            Log.i("kongyi0605", "sheetSize = ${modelView?.sheetSize}")
        }
        modelView?.sheetSize = DataManager.sheetList.value!!.size
        for (i in 0 until DataManager.sheetList.value!!.size) {
            Log.i("kongyi0605", "i = $i")

            val textView = DataManager.sheetList.value!![i].getTabTitleView()
            textView?.let {
                modelView?.sheetOrder?.set(textView!!.id, i)
                textView?.setOnClickListener {
                    modelView?.switchFocusSheetInTab(it)
                    modelView?.sheetOrder?.get((it as TextView).id)?.let { it1 ->
                        //                                Toast.makeText(this, "it1 = " + it1, Toast.LENGTH_SHORT).show()
                        vpPager.setCurrentItem(it1, true)
                        modelView?.currentTabPosition = it1
                    }
                }
                textView?.setBackgroundColor(resources.getColor(R.color.colorDeactivatedSheet))

                if (textView != null) {
                    Log.i("kongyi0605", "addShowingSheetInTab will be called soon")
                    addShowingSheetInTab(textView)
                }
            }
        }
        if (modelView?.sheetSize!! > 0) {
            val currentTabTitleView = DataManager.sheetList.value!![0].getTabTitleView()
            Log.i("kongyi0421", "currentTabTitleView = " + currentTabTitleView)
            currentTabTitleView?.setBackgroundColor(
                resources.getColor(
                    R.color.colorActivatedSheet
                )
            )
        } else {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(this@MainActivity, "저장된 데이터가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        Log.d(TAG, "onCreateOptionsMenu")
        menuInflater.inflate(R.menu.memo_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected")
        when(item.itemId) {
            R.id.menuSaveOptionBtn-> saveAllIntoDB()
            R.id.menuEditSheetNameBtn-> makeDialogAndEditSheetName()
            R.id.menuDeleteSheetBtn-> deleteCurrentSheet()
            R.id.menuTextSizeIncreaseBtn-> modelView?.contentTextSizeIncrease()
            R.id.menuTextSizeDecreaseBtn-> modelView?.contentTextSizeDecrease()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun isTargetLastElement(target: Int):Boolean {
        return target == modelView?.sheetSize!!-1
    }

    private fun deleteCurrentSheet() {
        if (modelView!!.size <= 0) return
        for (i in 0 until modelView!!.size) {
            val textViewId: Int = DataManager.sheetList.value?.get(i)?.getId()!!
            val order = modelView?.sheetOrder?.get(textViewId)
            if (order != null && order >= modelView?.currentTabPosition!!) {
                modelView?.sheetOrder?.set(textViewId, order-1)
            }
        }
        val target = modelView?.currentTabPosition!!
        val idsOld = modelView?.createIdSnapshot()
        modelView?.removeAt(target)
        val idsNew = modelView?.createIdSnapshot()
        DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = idsOld!!.size
            override fun getNewListSize(): Int = idsNew!!.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                idsOld!![oldItemPosition] == idsNew!![newItemPosition]
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                areItemsTheSame(oldItemPosition, newItemPosition)
        }, true).dispatchUpdatesTo(vpPager.adapter!!)

        modelView?.removeShowingSheetInTab(modelView?.currentTabTitleView as View)
        if (isTargetLastElement(target)) {
            if (modelView?.sheetSize!! > 1) {
                switchFocusSheetInTab(target - 1)
            }
        } else {
            switchFocusSheetInTab(target)
        }
        modelView?.sheetSize = modelView?.sheetSize!!-1

    }

    override fun onBackPressed() {
        super.onBackPressed()
        saveAllIntoDB()
    }

    override fun onDestroy() {
        super.onDestroy()
        tts?.close()
        softKeyboard?.unRegisterSoftKeyboardCallback();
    }

    private fun switchFocusSheetInTab(position: Int) {
        modelView?.switchFocusSheetInTab(position)
        modelView?.updateFragmentToSheets()
        tabOuter.requestFocus()
        //showAllData("switchFocusSheetInTab")
    }

    private fun makeDialogAndEditSheetName() {
        // make Dialog
        val dlg: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
        val ad: AlertDialog = dlg.create()
        ad.setTitle("Edit Name") //제목
        val inflater: LayoutInflater = LayoutInflater.from(applicationContext)
        val view = inflater.inflate(R.layout.dialog, findViewById(R.id.root_layout), false)
        ad.setView(view) // 메시지
        // set New Sheet Name if the confirm button is clicked.
        view.findViewById<Button>(R.id.dialogConfirmBtn).setOnClickListener {
            for (i in 1..DataManager.sheetList.value!!.size) {
                if (modelView?.currentTabTitleView?.id == DataManager.sheetList.value!!.get(i - 1)?.getId()) {
                    DataManager.sheetList.value!!.get(i - 1)?.getTabTitleView()?.text = view.findViewById<EditText>(R.id.dialogEditBox).text
                    DataManager.sheetList.value!!.get(i - 1)?.setName(view.findViewById<EditText>(R.id.dialogEditBox).text.toString())
                    break
                }
            }
            ad.dismiss()
        }
        ad.show()
    }

    fun onClickPlusIcon(view: View) {
        val context:Context = application
        val textView = TabTextView(context)
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        textView.layoutParams = params
        textView.text = "newSheet"
        textView.id = modelView?.sheetLastId!! + 1
        modelView?.sheetLastId = modelView?.sheetLastId!! + 1
//        textView.background = getDrawable(R.drawable.edge)
        textView.setBackgroundColor(context.resources.getColor(R.color.colorActivatedSheet))
//        textView.typeface = resources.getFont(R.font.whj000f0cb5)
        Log.d(TAG, "view textView = $textView")
        modelView?.sheetSize = modelView?.sheetSize!!+1
        modelView?.sheetOrder?.set(textView.id, modelView?.sheetSize!!-1)

        textView.setOnClickListener {
            modelView?.switchFocusSheetInTab(it)
            vpPager.setCurrentItem(modelView?.sheetOrder!![it.id]!!, true)
            Log.d(TAG, "pos = " + modelView?.sheetOrder!![it.id]!!)
        }
        modelView?.addNewSheet(vpPager, textView)
        addShowingSheetInTab(textView)
    }

    private fun findInput() {

    }

    private fun findNext() {

    }

    private fun initializeHotKey() {
        Log.d(TAG, "initializeHotKey")

/*        val editText: EditText? = findViewById(R.id.editText)
        editText?.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_F -> if (event?.isCtrlPressed == true) {
                        Log.d("kongyi123", "ctrl + f pressed")
                        Toast.makeText(v?.context, "ctrl + f pressed", Toast.LENGTH_SHORT).show()
                        findInput()
                        //editText.setSelection(1,5)
                    }
                    KeyEvent.KEYCODE_F3 -> {
                        Toast.makeText(v?.context, "F3 pressed", Toast.LENGTH_SHORT).show()
                        findNext();
                    }
                    KeyEvent.KEYCODE_F2 -> {
                        Toast.makeText(v?.context, "F2 saved", Toast.LENGTH_SHORT).show()
                        save();
                    }
                    KeyEvent.KEYCODE_PLUS -> if (event?.isShiftPressed == true) {
                        Toast.makeText(v?.context, "plus pressed", Toast.LENGTH_SHORT).show()
                        viewModel?.currentTextSize = viewModel?.currentTextSize!! + 1;
                        editText.textSize  = viewModel?.currentTextSize!!
                    }
                    KeyEvent.KEYCODE_MINUS -> if (event?.isShiftPressed == true) {
                        Toast.makeText(v?.context, "minus pressed", Toast.LENGTH_SHORT).show()
                        viewModel?.currentTextSize = viewModel?.currentTextSize!! - 1;
                        editText.textSize = viewModel?.currentTextSize!!
                    }

                    KeyEvent.KEYCODE_F5 -> {
                        tts?.input(editText.text.toString())
                        tts?.start()
                    }
                    KeyEvent.KEYCODE_PAGE_UP -> if (event?.isCtrlPressed == true) {
                        if (tts != null && tts!!.speed < 3) {
                            tts!!.speed += 0.25f;
                            Toast.makeText(v?.context, "speed = " + tts!!.speed, Toast.LENGTH_SHORT).show()
                            tts?.setPitch()
                            tts?.setSpeechRate()
                        }
                    }
                    KeyEvent.KEYCODE_PAGE_DOWN -> if (event?.isCtrlPressed == true) {
                        if (tts != null && tts!!.speed > 1) {
                            tts!!.speed -= 0.25f;
                            Toast.makeText(v?.context, "speed = " + tts!!.speed, Toast.LENGTH_SHORT).show()
                            tts?.setPitch()
                            tts?.setSpeechRate()
                        }
                    }
                }
                true
            }
            false
        }
    */
    }

    /** Save data (the contents of sheets and so on)
     * 1. Move all data from adapterSheetFragmentArray to Sheets
     * 2. Set all data of sheets to PreferenceManager
     */
    private fun saveAllIntoDB() {
        Log.d(TAG, "save")
        modelView?.saveAllIntoDB()
    }

    /** Clear All sheets and data
     * - It works when CELAR variable set to true in code
     */
    private fun clearData() {
        if (CLEAR) {
            PreferenceManager.setInt(this, "sheetCount", 0)
            PreferenceManager.setInt(this, "sheetIdCount", 0)
            Toast.makeText(this, "초기화 완료", Toast.LENGTH_SHORT).show()
            return
        }
    }

    private fun settingSIP(mContext: MainActivity) {
        rootLayout = mContext.findViewById<LinearLayout>(R.id.root_layout)
        controlManager = mContext.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        softKeyboard = SoftKeyboard(rootLayout, controlManager, mContext)
        softKeyboard!!.setSoftKeyboardCallback(object : SoftKeyboard.SoftKeyboardChanged {
            override fun onSoftKeyboardHide() {
                Log.d(TAG, "keyboard hided")
            }

            override fun onSoftKeyboardShow() {
                Log.d(TAG, "keyboard onSoftKeyboardShow")
            }
        })
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
}
