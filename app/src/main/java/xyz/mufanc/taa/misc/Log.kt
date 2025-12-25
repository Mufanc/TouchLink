package xyz.mufanc.taa.misc

import android.util.Log

object Log {

    fun d(tag: String, message: String) {
        println("[$tag] $message")
    }

    fun e(tag: String, message: String, err: Throwable) {
        println("[$tag] $message\n${Log.getStackTraceString(err)}")
    }
}