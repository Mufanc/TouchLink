package xyz.mufanc.taa.server

import com.alibaba.fastjson2.into
import xyz.mufanc.taa.input.InputDispatcher
import xyz.mufanc.taa.input.TouchAction
import xyz.mufanc.taa.misc.Log
import java.io.BufferedReader
import java.io.InputStreamReader

class StdioServer {

    companion object {
        private const val TAG = "StdioServer"
    }

    @Volatile
    private var running = false

    fun serve() {
        running = true
        val reader = BufferedReader(InputStreamReader(System.`in`))

        try {
            Log.d(TAG, "server is running...")

            while (running) {
                val line = reader.readLine() ?: break

                if (line.isBlank()) {
                    continue
                }

                try {
                    val actions = line.into<List<TouchAction>>().map { it.downcast() }
                    Log.d(TAG, "actions: $actions")
                    InputDispatcher.dispatchActions(actions)
                } catch (err: Exception) {
                    Log.e(TAG, "failed to process input", err)
                }
            }
        } catch (err: Exception) {
            Log.e(TAG, "failed to read from stdin", err)
        } finally {
            Log.d(TAG, "server stopped")
        }
    }

    fun stop() {
        running = false
    }
}