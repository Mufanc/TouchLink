package xyz.mufanc.taa

import kotlinx.coroutines.runBlocking

object Main {

    private const val TAG = "TouchLink"

    @JvmStatic
    fun main(args: Array<String>) {
        Thread.setDefaultUncaughtExceptionHandler { th, err ->
            LogProxy.e(TAG, "Uncaught exception on thread: $th", err)
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
