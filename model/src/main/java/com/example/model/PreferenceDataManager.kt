package com.example.model

import android.content.Context
import android.util.Log
import com.example.model.data.CalendarFilter
import com.google.gson.Gson

class PreferenceDataManager(private val context:Context) {
    /**
     * float 값 로드
     * @param key
     * @return
     */
    fun getFloat(key: String?): Float {
        return PreferenceManager.getFloat(context, key)
    }

    /**
     * int 값 로드
     * @param key
     * @return
     */
    fun getInt(key: String?): Int {
        return PreferenceManager.getInt(context, key)
    }

    /**
     * int 값 저장
     * @param key
     * @param value
     */
    fun setInt(key: String?, value: Int) {
        PreferenceManager.setInt(context, key, value)
    }

    /**
     * float 값 저장
     * @param key
     * @param value
     */
    fun setFloat(key: String?, value: Float) {
        PreferenceManager.setFloat(context, key, value)
    }

    /**
     * String 값 로드
     * @param key
     * @return
     */
    fun getString(key: String?): String? {
        return PreferenceManager.getString(context, key)
    }

    /**
     * String 값 저장
     * @param key
     * @param value
     */
    fun setString(key: String?, value: String?) {
        PreferenceManager.setString(context, key, value)
    }

    /**
     * CalendarFilter 값 로드
     * @param key
     * @return
     */
    fun getCalendarFilter(key: String?): CalendarFilter? {
        Log.i("kongyi0515-2", "getCalendarFilter()")
        val jsonStringValue = PreferenceManager.getString(context, key)
        val gson = Gson()
        Log.i("kongyi0515-2", "jsonStringValue = $jsonStringValue")
        return gson.fromJson(jsonStringValue, CalendarFilter::class.java)
    }

    /**
     * CalendarFilter 값 저장
     * @param key
     * @param value
     */
    fun setCalendarFilter(key: String?, value: CalendarFilter?) {
        Log.i("kongyi0515-2", "setCalendarFilter()")
        val gson = Gson()
        val jsonStringValue = gson.toJson(value)
        Log.i("kongyi0515-2", "jsonStringValue = $jsonStringValue")
        PreferenceManager.setString(context, key, jsonStringValue)
    }
}