package xyz.mufanc.taa.input

import android.graphics.Point
import java.io.Serializable

open class TouchAction : Serializable {

    lateinit var action: String
    var delay: Long = 0
    var duration: Long = 0
    lateinit var args: Array<Int>

    data class Touch(val point: Point) : TouchAction()
    data class Swipe(val points: List<Point>) : TouchAction()

    fun downcast(): TouchAction {
        val result = when (action) {
            "touch" -> Touch(Point(args[0], args[1]))
            "swipe" -> {
                val points = ArrayList<Point>(args.size / 2)

                for (i in args.indices step 2) {
                    points.add(Point(args[i], args[i + 1]))
                }

                Swipe(points)
            }
            else -> throw IllegalArgumentException("Unsupported action: $action")
        }

        result.delay = delay
        result.duration = duration

        return result
    }

    override fun toString(): String {
        return "TouchAction(action=$action, delay=$delay, duration=$duration, args=${args.contentToString()})"
    }
}
