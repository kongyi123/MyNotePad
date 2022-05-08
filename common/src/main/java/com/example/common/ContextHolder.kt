package com.example.common

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
object ContextHolder {

    private var mContext:Context? = null
    private var mPhoneNumber:String? = null
    fun setContext(context:Context?) {
        mContext = context
    }

    fun getContext():Context? {
        return mContext
    }

    fun setPhoneNumber(number:String) {
        mPhoneNumber = number
    }

    fun getPhoneNumber():String {
        return mPhoneNumber!!
    }
}