package no.danielzeller.blurbehind.animation

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.animation.PathInterpolator
import no.danielzeller.blurbehind.R
import no.danielzeller.blurbehind.extensions.interpolate
import no.danielzeller.blurbehind.extensions.onEnd
import no.danielzeller.blurbehind.extensions.onUpdate

const val CLIP_ANIM_DURATION = 800L
private const val ROTATE_SPEED = 0.3f
val scaleProgressBarInterpolator = PathInterpolator(.63f, .3f, 0f, .99f)

class LoaderImageView(context: Context?, attrs: AttributeSet?) : AppCompatImageView(context, attrs) {

    private var introAnim: ValueAnimator? = null
    private var scale = 1f

    var isLoaderVisible = false
        set(value) {
            field = value
            invalidate()
        }

    private val paint = Paint()
    private val color1: Int
    private val color2: Int
    private val color3: Int
    private val dotSize: Float

    private val circlePath1 = Path()
    private val circlePath2 = Path()
    private val circlePath3 = Path()
    private val clipPath = Path()

    private lateinit var pm1: PathMeasure
    private lateinit var pm2: PathMeasure
    private lateinit var pm3: PathMeasure

    private var clipPathScale = 1f
    private var rotateAmount = 0f

    private val fc = FrameRateCounter()
    private val pathPosition = floatArrayOf(0f, 0f)

    init {
        paint.isAntiAlias = true
        color1 = resources.getColor(R.color.progressColor1, null)
        color2 = resources.getColor(R.color.progressColor2, null)
        color3 = resources.getColor(R.color.progressColor3, null)
        paint.strokeWidth = resources.getDimension(R.dimen.progressCircleStrokeWidth)
        dotSize = resources.getDimension(R.dimen.progressDotRadius)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        buildCirclePaths()
    }

    private fun buildCirclePaths() {
        circlePath1.reset()
        circlePath2.reset()
        circlePath3.reset()
        circlePath1.addCircle(width / 2f, height / 2f, resources.getDimension(R.dimen.progressCircleRadius1), Path.Direction.CW)
        circlePath2.addCircle(width / 2f, height / 2f, resources.getDimension(R.dimen.progressCircleRadius2), Path.Direction.CW)
        circlePath3.addCircle(width / 2f, height / 2f, resources.getDimension(R.dimen.progressCircleRadius3), Path.Direction.CW)
        pm1 = PathMeasure(circlePath1, true)
        pm2 = PathMeasure(circlePath2, true)
        pm3 = PathMeasure(circlePath3, true)
    }

    fun introAnimate() {
        introAnim = ValueAnimator.ofFloat(1f, 0f).setDuration(CLIP_ANIM_DURATION).onUpdate { value ->
            val animatedValue = value as Float
            scale = 1f + animatedValue
            clipPathScale = animatedValue
        }.interpolate(scaleProgressBarInterpolator).onEnd { isLoaderVisible = false }
        introAnim!!.start()
    }

    override fun onDraw(canvas: Canvas) {
        val count = canvas.save()
        canvas.scale(scale, scale, (width / 2).toFloat(), (height / 2).toFloat())
        super.onDraw(canvas)
        canvas.restoreToCount(count)

        drawLoader(canvas)
    }

    private fun drawLoader(canvas: Canvas) {
        if (isLoaderVisible) {
            val count = canvas.save()

            clipLoader(canvas)
            increaseRotation()

            canvas.drawColor(Color.BLACK)
            drawCircles(canvas)

            canvas.restoreToCount(count)

            invalidate()
        }
    }

    private fun drawCircles(canvas: Canvas) {
        canvas.rotate(-90f, width / 2f, height / 2f)
        val clipScaleParallax = (1f - clipPathScale) * 0.4f
        canvas.scale(clipPathScale + clipScaleParallax, clipPathScale + clipScaleParallax, width / 2f, height / 2f)
        drawPath(canvas, circlePath1, color1, rotateAmount * 3f, pm1)
        drawPath(canvas, circlePath2, color2, rotateAmount * 2f, pm2)
        drawPath(canvas, circlePath3, color3, rotateAmount, pm3)
    }

    private fun increaseRotation() {
        rotateAmount += fc.timeStep() * ROTATE_SPEED
        if (rotateAmount > 1f) rotateAmount = 0f
    }

    private fun clipLoader(canvas: Canvas) {
        if (clipPathScale != 1f) {
            clipPath.reset()
            clipPath.addCircle(width / 2f, height / 2f, width * 0.75f * clipPathScale, Path.Direction.CW)
            canvas.clipPath(clipPath)
        }
    }

    private fun drawPath(canvas: Canvas, path: Path, color: Int, rotationAmount: Float, pm: PathMeasure) {
        paint.style = Paint.Style.STROKE
        paint.color = color
        canvas.drawPath(path, paint)
        paint.style = Paint.Style.FILL

        pm.getPosTan(pm.length * (rotationAmount % 1f), pathPosition, null)
        canvas.drawCircle(pathPosition[0], pathPosition[1], dotSize, paint)
    }

    fun cancelIntroAnim() {
        introAnim?.cancel()
        scale = 1f
        clipPathScale = 1f
    }
}