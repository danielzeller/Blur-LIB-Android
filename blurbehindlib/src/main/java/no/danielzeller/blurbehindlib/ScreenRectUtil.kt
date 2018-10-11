package no.danielzeller.blurbehindlib

import android.graphics.Rect
import android.view.View

class ScreenRectUtil {
    companion object {
        fun getScreenRect(view: View): Rect {
            var src = FloatArray(8)
            var dst = floatArrayOf(0f, 0f, view.width.toFloat(), 0f, 0f, view.height.toFloat(), view.width.toFloat().toFloat(), view.height.toFloat())
            val matrix = view.getMatrix()

            matrix.mapPoints(src, dst)
            val rect = Rect((src[0] + view.left).toInt(), (src[1] + view.top).toInt(), (src[6] + +view.left).toInt(), (src[7] + +view.top).toInt())
            return rect
        }
    }
}