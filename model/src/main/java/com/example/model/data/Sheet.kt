package com.example.model.data

import android.widget.TextView

class Sheet {
    private var name: String? = null
    private var content: String? = null
    private var id: Int? = null
    private var textSize: Float? = null

    constructor(id: Int?, name: String?, content: String?, textSize: Float?) {
        this.id = id
        this.name = name
        this.content = content
        this.textSize = textSize
    }

    constructor() {}

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

    fun setName(text: String) {
        this.name = text
    }

    fun getTextSize(): Float? {
        return this.textSize
    }

    fun setTextSize(size:Float) {
        this.textSize = size
    }
}