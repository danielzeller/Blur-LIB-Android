package no.opengl.danielzeller.opengltesting.opengl.util

import android.os.SystemClock

object FrameRateCounter {

    private var lastTime: Long = 0
    var globalTime = 0f
    var deltaTime = 0f
    var deltaTime2 = 0.0f


    fun timeStep(): Float {

        val time = SystemClock.uptimeMillis()
        val timeDelta = time - lastTime
        deltaTime = if (lastTime > 0.0f) timeDelta / 1000.0f else 0.0f
        globalTime += deltaTime
        lastTime = time
        deltaTime2 += (deltaTime - deltaTime2) * 0.1f
        return deltaTime
    }
}
