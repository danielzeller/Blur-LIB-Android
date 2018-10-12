package no.danielzeller.blurbehind.animation

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.animation.PathInterpolator
import no.danielzeller.blurbehind.extensions.interpolate
import no.danielzeller.blurbehind.extensions.onUpdate

class ScaleInImageView(context: Context?, attrs: AttributeSet?) : AppCompatImageView(context, attrs) {


    fun introAnimate() {
        ValueAnimator.ofFloat(1.3f, 1f).setDuration(1000L).onUpdate { value ->
            scale = value as Float
            invalidate()
        }.interpolate(PathInterpolator(.19f,.72f,.37f,.94f)).start()
    }

    private var scale = 1f
    override fun onDraw(canvas: Canvas) {
        canvas.scale(scale, scale, (width / 2).toFloat(), (height / 2).toFloat())
        super.onDraw(canvas)
    }
}