package xyz.mufanc.taa

import android.net.LocalServerSocket
import android.net.LocalSocket
import kotlinx.coroutines.*
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
                LogProxy.d(TAG, "server is running...")

                while (running) {
                    try {
                        val client = socket.accept()

                        scope.launch {
                            handle(client)
                        }
                    } catch (err: IOException) {
                        if (running) {
                            LogProxy.e(TAG, "failed to accept", err)
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

            LogProxy.d(TAG, "handle client: uid=${peer.uid}, pid=${peer.pid}")

            socket.use { client ->
                val data = client.inputStream.use { it.readBytes().decodeToString() }

                LogProxy.d(TAG, "data: ${data.trim()}")
            }
        } catch (err: IOException) {
            LogProxy.e(TAG, "failed to handle client", err)
        }
    }

    fun stop() {
        running = false

        try {
            socket.close()
        } catch (err: IOException) {
            LogProxy.e(TAG, "failed to close socket", err)
        }

        scope.cancel()
    }

    private fun cleanup() {
        try {
            socket.close()
        } catch (err: IOException) {
            LogProxy.e(TAG, "failed to close socket", err)
        }
    }
}
