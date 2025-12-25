package xyz.mufanc.taa.server

import android.net.LocalServerSocket
import android.net.LocalSocket
import com.alibaba.fastjson2.into
import com.alibaba.fastjson2.parseArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.mufanc.taa.input.InputDispatcher
import xyz.mufanc.taa.input.TouchAction
import xyz.mufanc.taa.misc.Log
import java.io.IOException

class TouchServer {

    companion object {
        private const val TAG = "TouchServer"
    }

    private lateinit var socket: LocalServerSocket
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Volatile
    private var running = false

    suspend fun serve() {
        socket = LocalServerSocket("touchlink")
        running = true

        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "server is running...")

                while (running) {
                    try {
                        val client = socket.accept()

                        scope.launch {
                            handle(client)
                        }
                    } catch (err: IOException) {
                        if (running) {
                            Log.e(TAG, "failed to accept", err)
                        }
                        break
                    }
                }
            } finally {
                cleanup()
            }
        }
    }

    private suspend fun handle(socket: LocalSocket) {
        try {
            val peer = socket.peerCredentials

            Log.d(TAG, "handle client: uid=${peer.uid}, pid=${peer.pid}")

            socket.use { client ->
                val data = client.inputStream.use { it.readBytes().decodeToString() }
                val actions = data.into<List<TouchAction>>().map { it.downcast() }

                Log.d(TAG, "actions: $actions")

                InputDispatcher.dispatchActions(actions)
            }
        } catch (err: IOException) {
            Log.e(TAG, "failed to handle client", err)
        }
    }

    fun stop() {
        running = false

        try {
            socket.close()
        } catch (err: IOException) {
            Log.e(TAG, "failed to close socket", err)
        }

        scope.cancel()
    }

    private fun cleanup() {
        try {
            socket.close()
        } catch (err: IOException) {
            Log.e(TAG, "failed to close socket", err)
        }
    }
}