package com.example.model

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.example.common.MyNotification
import com.example.common.WidgetProvider
import com.example.model.data.History
import com.example.model.data.Notice
import com.example.common.data.Schedule
import com.example.common.Utils
import com.example.model.data.Sheet
import com.example.model.view.TabTextView
import com.google.firebase.database.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
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
        if (context.let { ContextCompat.checkSelfPermission(it, Manifest.permission.READ_PHONE_STATE) }
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(tt, arrayOf(Manifest.permission.READ_PHONE_STATE),1004)

        } else {
            try {
                result = (tt.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).line1Number.toString()
            } catch (e:NullPointerException) {
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

    fun get14daysSchedule(context: Context, id_list: String, intent: Intent) {
        val scheduleList = ArrayList<Schedule>()
        val sortByAge:Query = FirebaseDatabase.getInstance().reference.child(id_list)
        sortByAge.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.i("kongyi0414", "get14daysSchedule - onchanged")
                scheduleList.clear()
                var start = false
                var cnt = 0
                var str = ""
                for(postSnapshot in snapshot.children) {
                    if (!postSnapshot.exists()) {
                        continue
                    }
                    for (postPostSnapshot in postSnapshot.children) {
                        Log.i("kongyi1220", "key = " + postPostSnapshot.key.toString())
                        val get = postPostSnapshot.getValue(FirebasePost::class.java)
                        Log.i("kongyi1220", "title = ${get?.title}, content = ${get?.content}, id = ${get?.id}")

                        get?.id?.let {
                            if (!start) {
                                val cal = Calendar.getInstance()
                                cal.timeInMillis = System.currentTimeMillis()
                                val dateOfToday = Utils.getDateFromCalToString(cal)
                                if (get.date >= dateOfToday) {
                                    start = true
                                    Log.i("kongyi0414", "${get.date}/${dateOfToday}")
                                }
                            }
                            if (start && cnt <= 14) {
                                Log.i("kongyi0414", "in start && cnt <=14 date = ${get.date.toString()}")
                                cnt ++
                                str += Utils.convDBdateToShown(get.date.toString()) + " " + get.title + " " + get.content + "\n"
                                scheduleList.add(
                                    Schedule(get.id,get.date,get.title,get.content,get.color)
                                )
                            }
                        }
                    }
                }
                Log.i("kongyi1220aa", "scheduleList = ${scheduleList}")
                // https://aroundck.tistory.com/39
                CoroutineScope(Dispatchers.Main).launch {
                    Log.i("kongyi1220aa", "view.post is called")
                    val wp = WidgetProvider()
                    wp.update(context, scheduleList, intent)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

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
                Log.i("kongyi1220", "onChanged")
                CoroutineScope(Dispatchers.Default).launch {
                    updateDataList(scheduleList, snapshot)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

    }

    // debounce logic.
    private var lastJob: Job? = null

    private suspend fun updateDataList(
        scheduleList: ArrayList<Schedule>,
        snapshot: DataSnapshot
    ) {
        if (lastJob != null) {
            lastJob!!.cancel()
        }
        lastJob = CoroutineScope(Dispatchers.Default).launch {
            delay(500)
            doUpdate(scheduleList, snapshot)
            lastJob = null
        }
    }

    private fun doUpdate(
        scheduleList: ArrayList<Schedule>,
        snapshot: DataSnapshot
    ) {
        scheduleList.clear()
        Log.i("kongyi0504", "scheduleList right after clear= {$scheduleList}")

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
            val newId = Utils.bytesToHex1(Utils.sha256(date+title+content))
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
        var puttingId = Utils.bytesToHex1(Utils.sha256(date+title+content))
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

    fun setSingleSheetOnRTDB(context:Context, i:Int, item: Sheet?) {
        val pdm = PreferenceDataManager(context)
        val sheetNameKey = "sheetName$i"
        val sheetContentKey = "sheetContent$i"
        val sheetIdKey = "sheetId$i"
        val sheetTextSizeKey = "sheetTextSize$i"

        putStringAtPathOnRTDB(sheetNameKey, item?.getName()!!)
        putStringAtPathOnRTDB(sheetContentKey, item?.getContent()!!)
        putStringAtPathOnRTDB(sheetIdKey, item?.getId().toString())
        putStringAtPathOnRTDB(sheetTextSizeKey, item?.getTextSize()!!.toString())
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

    suspend fun getSingleSheetFromRTDB(context:Context, i:Int):Sheet {
        Log.i("kongyi0420", "getSingleSheetFromRTDB")

//        val pdm = PreferenceDataManager(context)
        val sheetNameKey = "sheetName${i-1}"
        val sheetContentKey = "sheetContent${i-1}"
        val sheetIdKey = "sheetId${i-1}"
        val sheetTextSizeKey = "sheetTextSize${i-1}"
        var sheetNameValue = ""
        var sheetIdValue = "0"
        var sheetContentValue = ""
        var sheetTextSizeValue = 0f

        getStringFromRTDB(sheetIdKey).take(1).collect {
            Log.i("kongyi0420", "it = $it")
            if (it == "fail" || it == null || it == "null") {
                Log.i("kongyi0420", "getting sheetNameKey is failed")
            } else {
                sheetIdValue = it
                Log.i("kongyi0420", "it = $it")
            }
        }
        getStringFromRTDB(sheetNameKey).take(1).collect {
            Log.i("kongyi0420", "it = $it")
            if (it == "fail" || it == null || it == "null") {
                Log.i("kongyi0420", "getting sheetNameKey is failed")
            } else {
                sheetNameValue = it
                Log.i("kongyi0420", "it = $it")
            }
        }

        getStringFromRTDB(sheetContentKey).take(1).collect {
            Log.i("kongyi0420", "it = $it")
            if (it == "fail" || it == null || it == "null") {
                Log.i("kongyi0420", "getting sheetNameKey is failed")
            } else {
                sheetContentValue = it
                Log.i("kongyi0420", "it = $it")
            }
        }

        getStringFromRTDB(sheetTextSizeKey).take(1).collect {
            Log.i("kongyi0420", "it = $it")
            if (it == "fail" || it == null || it == "null") {
                Log.i("kongyi0420", "getting sheetNameKey is failed")
            } else {
                sheetTextSizeValue = it.toFloat()
                Log.i("kongyi0420", "it = $it")
            }
        }
        return Sheet(sheetIdValue.toInt(),
            sheetNameValue,
            sheetContentValue,
            sheetTextSizeValue)
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

    suspend fun getSheetCountFromRTDB(context:Context):Int {
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

    fun getSheetCount(context:Context):Int {
        val pdm = PreferenceDataManager(context)
        return pdm.getInt("sheetCount")
    }

    suspend fun getIdCountFromRTDB(context:Context):Int {
        var sheetIdCount = 0
        getStringFromRTDB("sheetIdCount").take(1).collect {
            if (it == "fail" || it == "null") {
                Log.i("kongyi1220dd", "getting sheetNameKey is failed")
            } else {
                sheetIdCount = it.toInt()
                Log.i("kongyi1220dd", "it = $it")
            }
        }
        return sheetIdCount
    }

    fun getIdCount(context:Context):Int {
        val pdm = PreferenceDataManager(context)
        return pdm.getInt("sheetIdCount")
    }

    fun loadNotepadData(context:Context) {
        Log.i("kongyi0509", "loadNotepadData")

        val items:MutableList<Sheet> = mutableListOf()
        CoroutineScope(Dispatchers.IO).launch {
            val sheetSize = getSheetCountFromRTDB(context)
            Log.i("kongyi0509", "sheetSize : $sheetSize")

            if (sheetSize > 0) {
                for (i in 1..sheetSize) {
                    val item:Sheet = getSingleSheetFromRTDB(context, i)
                    val sheetName = item.getName()
                    val sheetContent = item.getContent()
                    val sheetId:String = item.getId().toString()
                    var sheetTextSize:Float? = item.getTextSize()
                    if (sheetTextSize == -1.0f) {
                        sheetTextSize = 10.0f
                    }
                    val textView = TabTextView(context.applicationContext);
                    Log.i("kongyi0421", "sheetName : $sheetName")
                    items.add(Sheet(sheetId.toInt(), sheetName, sheetContent, textView, sheetTextSize))
                    val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    textView.layoutParams = params
                    textView.text = sheetName
                    textView.id = sheetId.toInt()
                    textView.setBackgroundColor(context.resources.getColor(R.color.colorDeactivatedSheet))
                }
            } //sheetList
            sheetList.postValue(items)
        }
    }
}