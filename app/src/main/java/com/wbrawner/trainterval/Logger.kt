package com.wbrawner.trainterval

import android.util.Log

interface Logger {
    val defaultTag: String
    fun v(tag: String = defaultTag, message: String)
    fun d(tag: String = defaultTag, message: String)
    fun i(tag: String = defaultTag, message: String)
    fun w(tag: String = defaultTag, message: String)
    fun e(tag: String = defaultTag, message: String, error: Throwable? = null)
}

class AndroidLogger(override val defaultTag: String) : Logger {
    override fun v(tag: String, message: String) {
        Log.v(tag, message)
    }

    override fun d(tag: String, message: String) {
        Log.d(tag, message)
    }

    override fun i(tag: String, message: String) {
        Log.i(tag, message)
    }

    override fun w(tag: String, message: String) {
        Log.w(tag, message)
    }

    override fun e(tag: String, message: String, error: Throwable?) {
        Log.e(tag, message, error)
    }
}
