package xyz.mufanc.taa

import kotlinx.coroutines.runBlocking
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import xyz.mufanc.taa.misc.Log
import xyz.mufanc.taa.server.SocketServer
import xyz.mufanc.taa.server.StdioServer
import java.util.concurrent.Callable
import kotlin.system.exitProcess

@Command(name = "touchlink")
class Main : Callable<Int> {

    companion object {
        private const val TAG = "TouchLink"

        @JvmStatic
        fun main(args: Array<String>) {
            Thread.setDefaultUncaughtExceptionHandler { th, err ->
                Log.e(TAG, "Uncaught exception on thread: $th", err)
            }

            val code = CommandLine(Main()).execute(*args)
            exitProcess(code)
        }
    }

    @Option(
        names = ["-m", "--mode"],
        description = ["Communication mode: socket or stdio (default: stdio)"],
        defaultValue = "stdio",
        converter = [ModeConverter::class]
    )
    lateinit var mode: Mode

    override fun call(): Int {
        when (mode) {
            Mode.STDIO -> runStdio()
            Mode.SOCKET -> runSocket()
        }

        return 0
    }

    private fun runStdio() {
        val server = StdioServer()

        try {
            server.serve()
        } finally {
            server.stop()
        }
    }

    private fun runSocket() {
        runBlocking {
            val server = SocketServer()

            try {
                server.serve()
            } finally {
                server.stop()
            }
        }
    }

    enum class Mode {
        STDIO, SOCKET
    }

    class ModeConverter : CommandLine.ITypeConverter<Mode> {
        override fun convert(value: String): Mode {
            return Mode.valueOf(value.uppercase())
        }
    }
}
