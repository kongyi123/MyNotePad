package com.example.model.view


import android.content.Context
import android.util.AttributeSet
import android.view.Gravity

class TabTextView : androidx.appcompat.widget.AppCompatTextView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        gravity = Gravity.CENTER
//                    textView.background = getDrawable(R.drawable.edge)
        setPadding(30, 0, 30, 0)
    }
}