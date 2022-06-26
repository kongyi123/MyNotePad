package com.example.model.data

import android.widget.EditText
import android.widget.TextView
import com.example.model.view.SheetFragment

class Sheet {
    private var name: String? = null
    private var content: String? = null
    private var id: Int? = null
    private var tabTitleView: TextView? = null
    private var textSize: Float = 10.0f
    private var sheetFragment: SheetFragment? = null
    private var editTextView: EditText? = null

    constructor(id: Int, name: String?, content: String?, textView: TextView?, textSize: Float, editTextView: EditText?) {
        this.id = id
        this.name = name
        this.content = content
        this.tabTitleView = textView
        this.textSize = textSize
        this.editTextView = editTextView
    }

    constructor(id: Int, name: String?, content: String?, textView: TextView?, textSize: Float) {
        this.id = id
        this.name = name
        this.content = content
        this.tabTitleView = textView
        this.textSize = textSize
    }

    constructor(id: Int?, name: String?, content: String?, textSize: Float) {
        this.id = id
        this.name = name
        this.content = content
        this.textSize = textSize
    }

    constructor() {}

    fun getSheetFragment(): SheetFragment? {
        return sheetFragment
    }

    fun setSheetFragment(sheetFragment: SheetFragment) {
        this.sheetFragment = sheetFragment
    }

    fun getId(): Int? {
        return id
    }

    fun getName(): String? {
        return name
    }

    fun getContent(): String? {
        return content
    }

    fun setContent(text: String?) {
        this.content = text
    }

    fun getTabTitleView(): TextView? {
        return this.tabTitleView
    }

    fun setTabTitleView(textView: TextView?) {
        this.tabTitleView = textView
    }

    fun getEditTextView(): EditText? {
        return this.editTextView
    }

    fun setEditTextView(editTextView: EditText?) {
        this.tabTitleView = editTextView
    }

    fun setName(text: String) {
        this.name = text
    }

    fun getTextSize(): Float {
        return this.textSize
    }

    fun setTextSize(size:Float) {
        this.textSize = size
        updateTextSizeToEditText()
    }

    private fun updateTextSizeToEditText() {
        if (sheetFragment != null) {
            sheetFragment?.editText?.textSize = textSize!!
        }
    }
}