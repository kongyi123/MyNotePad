package com.example.mynotepad.activity

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.mynotepad.R
import com.example.model.PreferenceManager
import com.example.mynotepad.utility.TTSpeech
import com.example.mynotepad.view.SheetFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val TAG = "kongyi123/MainActivity"
    private var modelView: MainViewModel? = null
    private var tts: TTSpeech? = null

    // option setting
    private val CLEAR = false
    private lateinit var tabOuter:LinearLayout
    private lateinit var vpPager:ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        modelView = ViewModelProvider(this)[MainViewModel::class.java]
        modelView?.vpPager = findViewById(R.id.vpPager)
        tts = TTSpeech(this)

        tabOuter = findViewById(R.id.tabOuter)
        clearData()

        if (modelView?.isFirstStart == true) {
            initializeDataForTheFirstTime()
        }

        tts?.initTTS()
        if (modelView?.sheetIdCount == 0) {
            modelView?.sheetIdCount = 15
        }

        modelView?.vpPager?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                //                Toast.makeText(ontext, "pos = " + position + " / viewModelSheetIdCount = " + viewModel?.sheetIdCount, Toast.LENGTH_SHORT).show()
                if (modelView?.size!! > 0 && position >= 0 && position < modelView?.sheetIdCount!!) {
                    Log.d(TAG, "registerOnPageChangeCallback : position = " + position)
                    switchFocusSheetInTab(position)
                    //(viewModel?.adapterViewPager as MainViewModel.MyPagerAdapter).adapterSheetFragmentArray[position].textSize = viewModel!!.sheets[position].getTextSize()
                }
                modelView?.currentTabPosition = position
            }
        })
        initializeHotKey()
    }

    private fun createViewPagerAdapter(): RecyclerView.Adapter<*> {
        val items = modelView
        return object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int): SheetFragment {
                val sheetFragment = SheetFragment()
                sheetFragment.initialize(items!!.items[position]?.getContent()!!, items!!.items[position].getTextSize()!!, position)
                items!!.items[position].setSheetFragment(sheetFragment)
                return sheetFragment
            }

            override fun getItemCount(): Int = items!!.size
            override fun getItemId(position: Int): Long = items!!.itemId(position)
            override fun containsItem(itemId: Long): Boolean = items!!.contains(itemId)
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
            val textViewId: Int = modelView?.items?.get(i)?.getId()!!
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

// 아래는 사장된 코드임... 처음 모르는 것을 배울때는, 예제를 그대로 따르는 것을 원칙으로 하자.
        // 아래 방식은 중간에 있는 항목에는 동작하지만, 마지막 끝 항에 대해서는 current page가 부적절하게 표시되는 오류가 있다.
        // 지금 보니 아래 방식도 구현 가능한 예제가 구글에서 제공하는 git에 제시되어있음.
//            items?.currentTabPosition = target-1
//        }
//        else {
//            Log.d(TAG, "currentTabPosition = " + target)
//            items?.removeAt(target)
//            vpPager.adapter!!.notifyDataSetChanged()
//            items?.removeShowingSheetInTab(items?.currentTabTitleView as View)
//            switchFocusSheetInTab(target)
//            items?.sheetSize = items?.sheetSize!!-1
//        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        saveAllIntoDB()
    }

    override fun onDestroy() {
        super.onDestroy()
        tts?.close()
    }

    private fun switchFocusSheetInTab(position: Int) {
        modelView?.switchFocusSheetInTab(position)
        modelView?.updateFragmentToSheets()
        tabOuter.requestFocus()
        showAllData("switchFocusSheetInTab")
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
            for (i in 1..modelView?.items!!.size) {
                if (modelView?.currentTabTitleView?.id == modelView?.items?.get(i - 1)?.getId()) {
                    modelView?.items?.get(i - 1)?.getTabTitleView()?.text = view.findViewById<EditText>(R.id.dialogEditBox).text
                    modelView?.items?.get(i - 1)?.setName(view.findViewById<EditText>(R.id.dialogEditBox).text.toString())
                    break
                }
            }
            ad.dismiss()
        }
        ad.show()
    }

    fun onClickPlusIcon(view: View) {
        modelView?.addNewSheet(modelView?.vpPager!!)
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

    /** Get datas from PreferenceManager
    * - e.g. sheetCount, sheetIdCount, Sheets, currentTextView
    */
    private fun initializeDataForTheFirstTime() {
        val mContext = this
        CoroutineScope(Dispatchers.Main).launch {
            Log.i("kongyi0420", "initializeDataForTheFirstTime")
            val result = modelView?.initialize(mContext, supportFragmentManager)
            if (result == false) {
                Toast.makeText(mContext, "데이터 초기화 실패", Toast.LENGTH_SHORT).show()
            }
            modelView?.vpPager?.adapter = createViewPagerAdapter()
        }
    }

    private fun showAllData(callingFunction: String) {
        Log.d(TAG, "showAllData calling function = $callingFunction")
        Log.d(TAG, "currentViewInTab = " + modelView?.currentTabTitleView!!.text)
        for (i in 1..modelView!!.items.size) {
            val text: String = modelView?.items!![i - 1].getContent().toString()

            val title: String = modelView!!.items[i - 1].getName()!!
            val viewId: Int = modelView!!.items[i - 1].getTabTitleView()!!.id
            val sheetId: Int = modelView!!.items[i - 1].getId()!!
            var length = text.length
            if (text.length >= 5) {
                length = 5
            }
            Log.d(TAG, "num = $i, content = ${text.substring(0, length)}, title = $title, viewId = $viewId sheetId = $sheetId")
        }
    }

}
