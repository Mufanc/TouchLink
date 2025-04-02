package xyz.mufanc.taa

import android.util.Log

object LogProxy {

    fun d(tag: String, message: String) {
        println("[$tag] $message")
    }

    fun e(tag: String, message: String, err: Throwable) {
        println("[$tag] $message\n${Log.getStackTraceString(err)}")
    }
}
