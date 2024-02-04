package com.example.model

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.example.common.ContextHolder
import com.example.common.MyNotification
import com.example.model.data.History
import com.example.model.data.Notice
import com.example.common.data.Schedule
import com.example.common.CommonUtils
import com.example.model.data.CalendarFilter
import com.example.model.data.Sheet
import com.example.model.view.TabTextView
import com.google.firebase.database.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*


object DataManager {
    val dataList: MutableLiveData<ArrayList<Schedule>> = MutableLiveData()
    var notice: MutableLiveData<String> = MutableLiveData()
    var hcnt: MutableLiveData<Long> = MutableLiveData()
    val hList: MutableLiveData<ArrayList<History>> = MutableLiveData()
    val sheetList: MutableLiveData<MutableList<Sheet>> = MutableLiveData()

    private var lineNumber:String = ""

    const val TYPE_HISTORY:String = "HISTORY"
    const val TYPE_SCHEDULE:String = "SCHEDULE"

    @SuppressLint("HardwareIds")
    fun getLineNumber(context:Context, tt:Activity):String {
        var result = "none"
        if (context.let { ContextCompat.checkSelfPermission(it, Manifest.permission.READ_PHONE_STATE) } != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(tt, arrayOf(Manifest.permission.READ_PHONE_STATE),1004)

        }

        if (context.let { ContextCompat.checkSelfPermission(it, Manifest.permission.READ_SMS) } != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(tt, arrayOf(Manifest.permission.READ_SMS),1004)

        }

        if (context.let { ContextCompat.checkSelfPermission(it, Manifest.permission.READ_PHONE_STATE) } == PackageManager.PERMISSION_GRANTED
            && context.let { ContextCompat.checkSelfPermission(it, Manifest.permission.READ_SMS) } == PackageManager.PERMISSION_GRANTED) {
            try {
                result = (tt.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).line1Number.toString()
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }
        }

        lineNumber = result
        return result

    }

    fun getNotice() {
        val query:Query = FirebaseDatabase.getInstance().reference.child("notice")
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.i("kongyi1234", "changed")
                if (snapshot.exists()) {
                    val get = snapshot.getValue(Notice::class.java)
                    notice.value = get?.content.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    fun getNewNumberForHistory() {
        val query:Query = FirebaseDatabase.getInstance().reference.child("history_cnt")
        Log.i("kongyi1220", "ref = ${query.ref}")
        var cnt:Long = 0
        var str:String? = null
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val get = snapshot.value

                Log.i("kyi1220", "content = [${get}]")
                str = get.toString()
                Log.i("kyi1220", "str = [${str}]")
                hcnt.postValue(str!!.toLong())
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun get14daysSchedule(context: Context, id_list: String, intent: Intent): Flow<ArrayList<Schedule>> = callbackFlow {
        val scheduleList = ArrayList<Schedule>()
        val sortByAge:Query = FirebaseDatabase.getInstance().reference.child(id_list)
        sortByAge.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                CoroutineScope(Dispatchers.Default).launch {


                    Log.i("kongyi0414", "get14daysSchedule - onchanged")
                    scheduleList.clear()
                    var start = false
                    var cnt = 0
                    var str = ""
                    for (postSnapshot in snapshot.children) {
                        if (!postSnapshot.exists()) {
                            continue
                        }
                        for (postPostSnapshot in postSnapshot.children) {
                            Log.i("kongyi1220", "key = " + postPostSnapshot.key.toString())
                            val get = postPostSnapshot.getValue(FirebasePost::class.java)
                            Log.i(
                                "kongyi1220",
                                "title = ${get?.title}, content = ${get?.content}, id = ${get?.id}"
                            )

                            get?.id?.let {
                                if (!start) {
                                    val cal = Calendar.getInstance()
                                    cal.timeInMillis = System.currentTimeMillis()
                                    val dateOfToday = CommonUtils.getDateFromCalToString(cal)
                                    if (get.date >= dateOfToday) {
                                        start = true
                                        Log.i("kongyi0414", "${get.date}/${dateOfToday}")
                                    }
                                }
                                if (start && cnt <= 14) {
                                    Log.i(
                                        "kongyi0414",
                                        "in start && cnt <=14 date = ${get.date.toString()}"
                                    )
                                    cnt++
                                    str += CommonUtils.convDBdateToShown(get.date.toString()) + " " + get.title + " " + get.content + "\n"
                                    scheduleList.add(Schedule(get.id, get.date, get.title, get.content, get.color))
                                }
                            }
                        }
                    }
                    Log.i("kongyi1220aa", "scheduleList = ${scheduleList}")
                    send(scheduleList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
        awaitClose {  }
    }

    private fun getBitmapFromView(v: View): Bitmap {
        val b = Bitmap.createBitmap(v.measuredWidth, v.measuredHeight, Bitmap.Config.ARGB_8888);
        Log.i("kongyi1220aa", "v.width = " + v.measuredWidth)
        Log.i("kongyi1220aa", "v.height = " + v.measuredHeight)
        val c = Canvas(b);
        v.draw(c);
        return b;
    }

    fun getAllHistoryData(context:Context) {
        val historyList = ArrayList<History>()
        val sortByAge:Query = FirebaseDatabase.getInstance().reference.child("history")
        sortByAge.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.i("kongyi0414", "onchanged")
                historyList.clear()
                for(postSnapshot in snapshot.children) {
                    if (!postSnapshot.exists()) {
                        continue
                    }
                    Log.i("kongyi1220", "key = " + postSnapshot.key.toString())
                    val get = postSnapshot.getValue(FirebasePostForH::class.java)
                    Log.i(
                        "kongyi1220",
                        "title = ${get?.arg2}, content = ${get?.arg3}, id = ${get?.arg1}"
                    )
                    get?.arg1?.let {
                        historyList.add(History(get.id, get.command, get.arg1, get.arg2, get.arg3))
                    }
                }
                hList.postValue(historyList)
                val notificationEnable = getNotificationState(context)
                if (notificationEnable) {
                    val content = decideNotifyText(historyList)
                    val subjectLineNumber = getOnlySubjectLineNumber(historyList)
                    if (lineNumber != subjectLineNumber) {
                        MyNotification.doNotify(context, content) // 이거 대신 broadcast 하도록 해야한다.
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

    }

    private fun getOnlySubjectLineNumber(historyList: ArrayList<History>): String {
        var number:String? = "none"
        val latestAction = historyList.get(historyList.lastIndex)
        when (latestAction.arg2) {
            "access" -> number = latestAction.arg4
            "pcal-schedule-new" -> number = latestAction.arg4
            "pcal-schedule-remove" -> number = latestAction.arg4
            "pcal-schedule-modify" -> number = latestAction.arg5
            "cal-schedule-new" -> number = latestAction.arg4
            "cal-schedule-remove" -> number = latestAction.arg4
            "cal-schedule-modify" -> number = latestAction.arg5
        }
        return number!!
    }

    private fun decideNotifyText(historyList: ArrayList<History>): String {
        var content = "none"
        val latestAction = historyList.get(historyList.lastIndex)
        when (latestAction.arg2) {
            "access" -> {
                //                    content =
                //                        "번호 = ${latestAction.arg1} 행위 = ${latestAction.arg2}" +
                //                                " 대상 = ${latestAction.arg3} 주체 = ${latestAction.arg4}"
                content = "휴대전화번호 [${latestAction.arg4}]가 [${latestAction.arg3}]에 접근하였습니다."
            }
            "pcal-schedule-new" -> {
                //                    content =
                //                        "번호 = ${latestAction.arg1} 행위 = ${latestAction.arg2}" +
                //                                " arg3 = ${latestAction.arg3} arg4 = ${latestAction.arg4}" +
                //                                " arg5 = ${latestAction.arg5}"
                content = "휴대전화번호 [${latestAction.arg4}]가 [${latestAction.arg2}] 동작을 했습니다.\n" +
                        "상세 내용 : { ${latestAction.arg3} }"
            }
            "pcal-schedule-remove" -> {
                //                    content =
                //                        "번호 = ${latestAction.arg1} 행위 = ${latestAction.arg2}" +
                //                                " arg3 = ${latestAction.arg3} arg4 = ${latestAction.arg4}" +
                //                                " arg5 = ${latestAction.arg5}"
                content = "휴대전화번호 [${latestAction.arg4}]가 [${latestAction.arg2}] 동작을 했습니다. \n" +
                        "상세 내용 : { ${latestAction.arg3} }"
            }
            "pcal-schedule-modify" -> {
                content = "휴대전화번호 ${latestAction.arg5}가 [${latestAction.arg2}] 동작을 했습니다. \n" +
                        "수정 전 내용 : { ${latestAction.arg3} } \n" +
                        "수정 후 내용 : { ${latestAction.arg4} }"
            }
            "cal-schedule-new" -> {
                //                    content =
                //                        "번호 = ${latestAction.arg1} 행위 = ${latestAction.arg2}" +
                //                                " arg3 = ${latestAction.arg3} arg4 = ${latestAction.arg4}" +
                //                                " arg5 = ${latestAction.arg5}"
                content = "휴대전화번호 [${latestAction.arg4}]가 [${latestAction.arg2}] 동작을 했습니다.\n" +
                        "상세 내용 : { ${latestAction.arg3} }"
            }
            "cal-schedule-remove" -> {
                //                    content =
                //                        "번호 = ${latestAction.arg1} 행위 = ${latestAction.arg2}" +
                //                                " arg3 = ${latestAction.arg3} arg4 = ${latestAction.arg4}" +
                //                                " arg5 = ${latestAction.arg5}"
                content = "휴대전화번호 [${latestAction.arg4}]가 [${latestAction.arg2}] 동작을 했습니다. \n" +
                        "상세 내용 : { ${latestAction.arg3} }"
            }
            "cal-schedule-modify" -> {
                content = "휴대전화번호 ${latestAction.arg5}가 [${latestAction.arg2}] 동작을 했습니다. \n" +
                        "수정 전 내용 : { ${latestAction.arg3} } \n" +
                        "수정 후 내용 : { ${latestAction.arg4} }"
            }
        }
        return content
    }

    fun getAllScheduleData(id_list:String) {
        val scheduleList = ArrayList<Schedule>()
        val sortByAge:Query = FirebaseDatabase.getInstance().reference.child(id_list)
        sortByAge.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.i("kongyi0521", "onChanged")
                CoroutineScope(Dispatchers.Default).launch {
                    updateDataList(scheduleList, snapshot)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

    }

    // debounce logic.
    private suspend fun updateDataList(
        scheduleList: ArrayList<Schedule>,
        snapshot: DataSnapshot
    ) {
        Log.i("kongyi0521", "updateDataList")
        Mutex().withLock {
            if (ContextHolder.lastJob != null) {
                Log.i("kongyi0521", "lastJob Canceled")
                ContextHolder.lastJob!!.cancel()
            }
        }
        ContextHolder.lastJob = CoroutineScope(Dispatchers.Default).launch {
            delay(500)
            doUpdate(scheduleList, snapshot)
            ContextHolder.lastJob = null
        }
    }

    private fun doUpdate(
        scheduleList: ArrayList<Schedule>,
        snapshot: DataSnapshot
    ) {
        scheduleList.clear()
        for (postSnapshot in snapshot.children) {
            if (!postSnapshot.exists()) {
                continue
            }
            for (postPostSnapshot in postSnapshot.children) {
                val get = postPostSnapshot.getValue(FirebasePost::class.java)
                get?.id?.let {
                    scheduleList.add(Schedule(get.id, get.date, get.title, get.content, get.color))
                }
            }
        }
        Log.i("kongyi0504", "scheduleList after adding = {$scheduleList}")
        dataList.postValue(scheduleList) // similar with postValue
    }


    fun getScheduleDataInDate(date:String): ArrayList<Schedule> {
        val list = ArrayList<Schedule>()
        if (dataList.value != null) {
            for (node in dataList.value!!) {
                if (node.date == date) {
                    list.add(node)
                }
            }
        }
        return list
    }

    fun getSingleScheduleById(String:Int): Schedule? {
        return null
    }

    fun removeSingleSchedule(id_list:String, date:String, id:String) { // color 넣어야 할지
        val ref = FirebaseDatabase.getInstance().reference
        //ref.child("/id_list/$date").setValue(null)
        Log.i("kongyi12200", "date = " + date + " / id = " + id)
        ref.child("/$id_list/$date/$id").removeValue()
    }

    fun removeDayAllSchedule(id_list:String, date:String) { // color 넣어야 할지
        val ref = FirebaseDatabase.getInstance().reference
        ref.child("/$id_list/$date").removeValue()
    }

    fun putSingleSchedule(id_list:String, date:String, title:String, content:String, color:String, id:String) {
        Log.i("kongyi1220A", "id = " + id)
        if (id == "no_id") {
            val newId = CommonUtils.bytesToHex1(CommonUtils.sha256(date+title+content))
            postFirebaseDatabaseForPutSchedule(id_list, true, newId, date, title, content, color)
        } else {
            postFirebaseDatabaseForPutSchedule(id_list,  false, id, date, title, content, color)
        }
    }


    fun putSingleHistory(context: Context, command:String, arg1:String = "", arg2:String = "", arg3:String = "", arg4:String = "", arg5:String = "") {
        Log.i("kongyi1220TT", "command = ${command}, arg1 = $arg1, arg2 = $arg2, arg3 = $arg3, arg4 = $arg4, arg5 = $arg5")
        // arg1 : type
        val newId = "${1 + hcnt.value!!}"
        //Utils.bytesToHex1(Utils.sha256(""+System.currentTimeMillis()))
        postFirebaseDatabaseForPutHistory(true, newId, command, arg1, arg2, arg3)
    }

    private fun postFirebaseDatabaseForPutSchedule(id_list:String, add: Boolean, id:String, date:String, title:String, content:String, color:String) {
        val mPostReference = FirebaseDatabase.getInstance().reference
        val childUpdates: MutableMap<String, Any?> = HashMap()
        var postValues: Map<String?, Any?>? = null
        Log.i("kongyi111", "add = $add / id = $id");
        var puttingId = CommonUtils.bytesToHex1(CommonUtils.sha256(date+title+content))
        if (!add) {
            puttingId = id
        }

        val post = FirebasePost(puttingId, title, content, date, color)
        postValues = post.toMap()
        childUpdates["/$id_list/$date/$id"] = postValues
        mPostReference.updateChildren(childUpdates)
    }
/*
    히스토리 기능
    1. 히스토리 마지막 넘버. - get/put
    2. System.millisec로 time를 만들고, 히스토리 넘버는 별도임.
    3. id 주소에 데이터를 입력하되 주입값으로 히스토리 넘버 포함시킴.
    4. 동시에 히스토리 마지막 넘버 put으로 update.
*/
    private fun postFirebaseDatabaseForPutHistory(add: Boolean, id:String, command:String, arg1:String, arg2:String, arg3:String) {
        val mPostReference = FirebaseDatabase.getInstance().reference
        val childUpdates: MutableMap<String, Any?> = HashMap()
        var postValues: Map<String?, Any?>? = null
        Log.i("kongyi111", "add = $add / id = $id");

        val post = FirebasePostForH(id, command, arg1, arg2, arg3)
        postValues = post.toMapForHistory()
        childUpdates["/history/${id}"] = postValues
        mPostReference.updateChildren(childUpdates)

        Log.i("kongyi222", "add = $add / id = $id");
        val childUpdates2: MutableMap<String, Any?> = HashMap()
        if (add) {
            Log.i("kongyi333", "add = $add / id = $id / hcnt = ${hcnt.value}");

//            postValues = post.toMapForHistoryCnt(hcnt.value!!+1)
            childUpdates2["/history_cnt"] = (id).toString()
            mPostReference.updateChildren(childUpdates2)
        }
    }


    fun setNotice(content: String) {
        postFirebaseDatabaseForPutSchedule(content)
        notice.value = content
    }

    fun postFirebaseDatabaseForPutSchedule(content: String) {
        val mPostReference = FirebaseDatabase.getInstance().reference
        val childUpdates: MutableMap<String, Any?> = HashMap()
        val result = HashMap<String, Any>()
        result["content"] = content
        childUpdates["/notice/"] = result
        mPostReference.updateChildren(childUpdates)
    }

    fun postFirebaseDatabaseStringAtKey(path:String, key: String, string: String) {
        val mPostReference = FirebaseDatabase.getInstance().reference
        val childUpdates: MutableMap<String, Any?> = HashMap()
        val result = HashMap<String, Any>()
        result[key] = string
        //childUpdates["/notice/"] = result
        childUpdates[path] = result
        mPostReference.updateChildren(childUpdates)
    }

    //---------------------------------------------------------------------------------------------------------------
    // For Notification feature

    fun getNotificationState(context:Context):Boolean {
        val pdm = PreferenceDataManager(context)
        if (pdm.getInt("notification") == 1) {
            return true
        }
        return false
    }

    fun setNotificationState(context:Context, state:Boolean) {
        val pdm = PreferenceDataManager(context)
        if (state) {
            pdm.setInt("notification", 1)
        } else {
            pdm.setInt("notification", 0)
        }
    }

    //---------------------------------------------------------------------------------------------------------------
    // Regarding SharedPrefrence

    fun getUpdateState(context:Context):Boolean {
        val pdm = PreferenceDataManager(context)
        if (pdm.getInt("updateEnable") == 1) {
            return true
        }
        return false
    }

    fun setUpdateState(context:Context, state:Boolean) {
        val pdm = PreferenceDataManager(context)
        if (state) {
            pdm.setInt("updateEnable", 1)
        } else {
            pdm.setInt("updateEnable", 0)
        }
    }

    //---------------------------------------------------------------------------------------------------------------
    // Regarding NotePad

    fun getAllSheetData(sheet_list:String, context: Context) {
        val sheetList = ArrayList<Sheet>()
        val sortByAge:Query = FirebaseDatabase.getInstance().reference.child(sheet_list)
        sortByAge.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.i("kongyi0606", "onChanged!!!!!! here!!!! watch it!!")
                CoroutineScope(Dispatchers.Default).launch {
                    updateSheetList(sheetList, snapshot, context)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

    }

    // debounce logic.
    private suspend fun updateSheetList(
        sheetList: ArrayList<Sheet>,
        snapshot: DataSnapshot,
        context: Context
    ) {
        Log.i("kongyi0605", "updateDataList")

        if (ContextHolder.lastJob != null) {
            Log.i("kongyi0605", "lastJob Canceled")
            ContextHolder.lastJob!!.cancel()
        }
        ContextHolder.lastJob = CoroutineScope(Dispatchers.Default).launch {
            delay(1000)
            doSheetUpdate(sheetList, snapshot, context)
            ContextHolder.lastJob = null
        }
    }

    private fun doSheetUpdate(
        sheetList: ArrayList<Sheet>,
        snapshot: DataSnapshot,
        context: Context
    ) {
        sheetList.clear()
        Log.i("kongyi0608", "sheetList right after clear= {$sheetList}")

        for (postSnapshot in snapshot.children) {
            if (!postSnapshot.exists()) {
                continue
            }
            if (!("sheetLastId" in postSnapshot.ref.toString() || "size" in postSnapshot.ref.toString())) {
                val get = postSnapshot.getValue(FirebaseSheetPost::class.java)
                get?.id?.let {
                    val sheetName = get.name
                    val textView = TabTextView(context.applicationContext)
                    val sheetId = get.id
                    Log.i("kongyi0608", "textview id!!!! = ${textView.id}")
                    val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    textView.layoutParams = params
                    textView.text = sheetName
                    textView.id = sheetId.toInt() // 동적 생성시 default는 무조건 0이 됨.
                    textView.setBackgroundColor(context.resources.getColor(R.color.colorDeactivatedSheet))
                    val editTextView = EditText(context.applicationContext)
                    val params2 = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    editTextView.layoutParams = params2
                    editTextView.setText(get.content)
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
                    sheetList.add(Sheet(get.id.toInt(), get.name, get.content, textView, get.textSize.toFloat(), editTextView))
                    Log.i("kongyi0608", "textview.id = ${textView.id}")
                }
            }
        }

        Log.i("kongyi0606", "!!!!!!!!!!!sheetList after adding ")
        this.sheetList.postValue(sheetList)
    }

    fun removeSingleSheet(sheet_list:String, id:String) { // color 넣어야 할지
        val ref = FirebaseDatabase.getInstance().reference
        ref.child("/$sheet_list/sheetId$id").removeValue()
    }

    fun putSingleSheet(sheet_list:String, id:Int, name:String, content:String, textSize:Float, nextSheetIdCount:Int) {
        Log.i("kongyi1220A", "id = " + id)
        if (id == -1) {
            val newId = nextSheetIdCount
            postFirebaseDatabaseForPutSheet(sheet_list, true, newId, name, content, textSize)
            postFirebaseDatabaseForPutSheetIdCount(sheet_list, nextSheetIdCount)
        } else {
            postFirebaseDatabaseForPutSheet(sheet_list,  false, id, name, content, textSize)
        }
    }

    private fun postFirebaseDatabaseForPutSheet(sheet_list:String, add: Boolean, id:Int, name:String, content:String, textSize:Float) {
        val mPostReference = FirebaseDatabase.getInstance().reference
        val childUpdates: MutableMap<String, Any?> = HashMap()
        var postValues: Map<String?, Any?>? = null


        val post = FirebaseSheetPost(id.toString(), name, content, textSize.toString())
        postValues = post.toMap()
        childUpdates["/$sheet_list/sheetId$id"] = postValues
        mPostReference.updateChildren(childUpdates)
    }


    fun clearSheetListFirebaseDatabase(sheet_list:String) {
        val mPostReference = FirebaseDatabase.getInstance().reference
        val childUpdates: MutableMap<String, Any?> = HashMap()
        var postValues: Map<String?, Any?>? = null
        childUpdates["/$sheet_list"] = postValues
        mPostReference.updateChildren(childUpdates)
    }


    private fun postFirebaseDatabaseForPutSheetCount(sheet_list:String, nextSheetCount:Int) {
        val mPostReference = FirebaseDatabase.getInstance().reference
        val childUpdates: MutableMap<String, Any?> = HashMap()
        childUpdates["sheetCount"] = nextSheetCount.toString()
        mPostReference.updateChildren(childUpdates)
    }

    private fun postFirebaseDatabaseForPutSheetIdCount(sheet_list:String, nextSheetIdCount:Int) {
        val mPostReference = FirebaseDatabase.getInstance().reference
        val childUpdates: MutableMap<String, Any?> = HashMap()
        childUpdates["sheetIdCount"] = nextSheetIdCount.toString()
        mPostReference.updateChildren(childUpdates)
    }

    fun setSingleSheet(context:Context, i:Int, item: Sheet?) {
        val pdm = PreferenceDataManager(context)
        val sheetNameKey = "sheetName$i"
        val sheetContentKey = "sheetContent$i"
        val sheetIdKey = "sheetId$i"
        val sheetTextSizeKey = "sheetTextSize$i"

        pdm.setString(sheetNameKey, item?.getName())
        pdm.setString(sheetContentKey, item?.getContent())
        pdm.setString(sheetIdKey, item?.getId().toString())
        pdm.setFloat(sheetTextSizeKey, item?.getTextSize()!!)
    }

    fun setSingleSheetOnRTDB(context:Context, i:Int, item: Sheet?, nextSheetLastCount:Int) {
        putSingleSheet("sheet_list", i, item?.getName()!!, item.getContent()!!, item.getTextSize()!!,  nextSheetLastCount)
    }

    fun setSheetCount(context:Context, size:Int) {
        val pdm = PreferenceDataManager(context)
        pdm.setInt("sheetCount", size)
    }

    fun setSheetCountOnRTDB(context:Context, size:Int) {
        putStringAtPathOnRTDB("sheetCount", size.toString())
    }

    fun setIdCount(context:Context, count:Int) {
        val pdm = PreferenceDataManager(context)
        pdm.setInt("sheetIdCount", count)
    }

    fun setIdCountOnRTDB(context:Context, count:Int) {
        putStringAtPathOnRTDB("sheetIdCount", count.toString())
    }

    fun getSingleSheet(context:Context, i:Int):Sheet {
        val pdm = PreferenceDataManager(context)
        val sheetNameKey = "sheetName$i"
        val sheetContentKey = "sheetContent$i"
        val sheetIdKey = "sheetId$i"
        val sheetTextSizeKey = "sheetTextSize$i"
        return Sheet(pdm.getString(sheetIdKey)?.toInt()!!,
            pdm.getString(sheetNameKey),
            pdm.getString(sheetContentKey),
            pdm.getFloat(sheetTextSizeKey))
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun getStringFromRTDB(key:String): Flow<String> = callbackFlow {
        val query:Query = FirebaseDatabase.getInstance().reference.child("$key")
        Log.i("kongyi0420", "ref = ${query.ref}")
        var str:String? = null
        val valueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                CoroutineScope(Dispatchers.Default).launch {
                    val get = snapshot.value
                    str = get.toString()
                    Log.i("kongyi0420", "onDataChange : " + get)
                    if (str == null) {
                        send("fail")
                    } else {
                        send(str!!)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                CoroutineScope(Dispatchers.Default).launch {
                    send("fail")
                }
            }
        }
        query.addValueEventListener(valueListener)
        awaitClose { query.removeEventListener(valueListener) }
    }

    private fun putStringAtPathOnRTDB(key:String, value: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val mPostReference = FirebaseDatabase.getInstance().reference
            val childUpdates: MutableMap<String, Any?> = HashMap()
            childUpdates["/$key"] = value
            mPostReference.updateChildren(childUpdates)
        }
    }

    suspend fun getSheetListSizeFromRTDB(context:Context, sheet_list:String):Int {
        var sheetCount = 0
        getStringFromRTDB("sheetCount").take(1).collect {
            if (it == "fail" || it == null || it == "null") {
                Log.i("kongyi0420", "getting sheetNameKey is failed")
            } else {
                sheetCount = it.toInt()
                Log.i("kongyi0420", "it = $it")
            }
        }
        return sheetCount
    }
//
//    fun getSheetCount(context:Context):Int {
//        val pdm = PreferenceDataManager(context)
//        return pdm.getInt("sheetCount")
//    }

    suspend fun getLastIdFromRTDB(context:Context, sheet_list:String):Int {
        var sheetLastId = 0
        getStringFromRTDB("sheetIdCount").take(1).collect {
            if (it == "fail" || it == "null") {
                Log.i("kongyi1220dd", "getting sheetNameKey is failed")
            } else {
                sheetLastId = it.toInt()
                Log.i("kongyi1220dd", "it = $it")
            }
        }
        return sheetLastId
    }

    fun getIdCount(context:Context):Int {
        val pdm = PreferenceDataManager(context)
        return pdm.getInt("sheetIdCount")
    }


    fun getLastFilterSettingState(context: Context): CalendarFilter? {
        val pdm = PreferenceDataManager(context)
        return pdm.getCalendarFilter("calendarFilterState")
    }

    fun setUpdateFilterSettingState(context: Context, calendarFilter: CalendarFilter) {
        val pdm = PreferenceDataManager(context)
        pdm.setCalendarFilter("calendarFilterState", calendarFilter)
    }
}