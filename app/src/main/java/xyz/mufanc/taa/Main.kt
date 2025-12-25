package xyz.mufanc.taa

import kotlinx.coroutines.runBlocking
import xyz.mufanc.taa.misc.Log
import xyz.mufanc.taa.server.TouchServer

object Main {

    private const val TAG = "TouchLink"

    @JvmStatic
    fun main(args: Array<String>) {
        Thread.setDefaultUncaughtExceptionHandler { th, err ->
            Log.e(TAG, "Uncaught exception on thread: $th", err)
        }

        runBlocking {
            asyncMain()
        }
    }

    suspend fun asyncMain() {
        val server = TouchServer()

        try {
            server.serve()
        } finally {
            server.stop()
        }
    }
}
