package xyz.mufanc.taa.input

import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import androidx.core.os.postAtTime
import androidx.core.os.postDelayed
import kotlin.math.sqrt

object InputDispatcher {

    private const val SWIPE_EVENT_PERIOD_MILLIS = 1000f / 60f  // 60Hz

    private val worker by lazy { HandlerThread("TouchLink-dispatcher").apply { start() } }
    private val handler by lazy { Handler(worker.looper) }

    private fun check() {
        require(Thread.currentThread() == worker)
    }

    private fun performTouch(action: TouchAction.Touch) {
        check()

        val id = TouchSimulator.down(action.point.x, action.point.y)
        val ts = SystemClock.uptimeMillis()

        handler.postAtTime(ts + action.duration) {
            TouchSimulator.up(id, action.point.x, action.point.y)
        }
    }

    private fun performSwipe(action: TouchAction.Swipe) {
        check()
        require(action.points.size >= 2)

        val numSegments = action.points.size - 1
        val segments = FloatArray(numSegments)
        var length = 0f

        for (i in 0 ..< numSegments) {
            val p1 = action.points[i]
            val p2 = action.points[i + 1]

            val dx = (p2.x - p1.x).toFloat()
            val dy = (p2.y - p1.y).toFloat()

            val dis = sqrt(dx * dx + dy * dy)

            segments[i] = dis
            length += dis
        }

        var duration = action.duration

        if (duration == 0L) {
            duration = (action.points.size - 1) * 200L
        }

        val numEvents = (duration / SWIPE_EVENT_PERIOD_MILLIS).toInt()
        val ts = SystemClock.uptimeMillis()

        val id = TouchSimulator.down(action.points[0].x, action.points[0].y)

        fun lerp(a: Int, b: Int, t: Float): Int {
            return (a.toFloat() + (b.toFloat() - a) * t).toInt()
        }

        for (i in 1 .. numEvents) {
            val target = length * i.toFloat() / numEvents

            var distance = 0f
            var index = 0

            while (index < numSegments && distance + segments[index] < target) {
                distance += segments[index]
                index++
            }

            val progress = if (segments[index] > 0) {
                (target - distance) / segments[index]
            } else {
                0f
            }

            val p1 = action.points[index]
            val p2 = action.points[index + 1]
            val x = lerp(p1.x, p2.x, progress)
            val y = lerp(p1.y, p2.y, progress)

            val time = ts + (i * SWIPE_EVENT_PERIOD_MILLIS).toLong()

            handler.postAtTime(time) {
                TouchSimulator.move(id, x, y)
            }
        }

        handler.postAtTime(ts + duration) {
            val point = action.points.last()
            TouchSimulator.up(id, point.x, point.y)
        }
    }

    fun dispatchActions(actions: List<TouchAction>) {
        // Todo: 这里加个等待，任务排队
        actions.forEach { action ->
            handler.postDelayed(action.delay) {
                when (action) {
                    is TouchAction.Touch -> performTouch(action)
                    is TouchAction.Swipe -> performSwipe(action)
                }
            }
        }
    }
}