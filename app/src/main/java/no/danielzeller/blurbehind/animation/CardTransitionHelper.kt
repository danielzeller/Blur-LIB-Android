package no.danielzeller.blurbehind.animation

import android.animation.ObjectAnimator
import android.animation.PointFEvaluator
import android.animation.ValueAnimator
import android.app.Activity
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.PointF
import android.graphics.Rect
import android.os.SystemClock
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.v7.widget.CardView
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.LinearInterpolator
import android.view.animation.PathInterpolator
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.card2.view.*
import kotlinx.android.synthetic.main.fragment_details.view.*
import no.danielzeller.blurbehind.*
import no.danielzeller.blurbehind.extensions.*
import no.danielzeller.blurbehindlib.BlurBehindLayout

private const val FADE_TEXT_DURATION = 300L
const val FADE_BARS_DURATION = 100L
const val MOVE_DURATION = 600L
const val TARGET_BLUR_RADIUS = 30f
const val TARGET_TEXT_BLUR_RADIUS = 120f
const val BACKGROUND_VIEWS_SCALED_DOWN_SIZE = 0.9f

val moveInterpolator = PathInterpolator(.52f, 0f, .18f, 1f)
val scaleInterpolator = PathInterpolator(.24f, 0f, .13f, 1f)

class FrameRateCounter {
    private var lastTime: Long = 0

    fun timeStep(): Float {
        val time = SystemClock.uptimeMillis()
        val timeDelta = time - lastTime
        val timeDeltaSeconds = if (lastTime > 0.0f) timeDelta / 1000.0f else 0.0f
        lastTime = time
        return Math.min(0.015f, timeDeltaSeconds)
    }
}

class CardTransitionHelper(private val cardRootView: ConstraintLayout, private val backgroundView: ViewGroup, val textContainer: View) {

    private val contrainSet = ConstraintSet()
    private val cardViewCenterPosition = floatArrayOf(0f, 0f)
    private val cardView: CardView
    private lateinit var movePath: Path
    private val originSize = PointF()
    private val targetSize = PointF()
    private var currentPathMoveProgress = 0f
    private val runningAnimations = ArrayList<ValueAnimator>()
    private val frameRateCounter = FrameRateCounter()
    private var isEnterAnimating = false

    init {
        this.cardView = cardRootView.getChildAt(0) as CardView
    }

    fun animateCardIn() {
        cardRootView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (cardRootView.image.drawable != null) {

                    var drawable = cardRootView.image.drawable
                    var targetWidth = backgroundView.width.toFloat()
                    var targetHeight = Math.min(targetWidth, (drawable.intrinsicHeight.toFloat() / drawable.intrinsicWidth.toFloat()) * targetWidth)

                    targetSize.set(targetWidth, targetHeight)
                    originSize.set(cardRootView.width.toFloat(), cardRootView.height.toFloat())

                    movePath = createMovePath()
                    animateCardPosition(1f)
                    animateCardSize(targetSize)
                    animateCardCornerRadius(0f)
                    scaleBackgroundView(BACKGROUND_VIEWS_SCALED_DOWN_SIZE, 0.5f, scaleInterpolator, -1.5f)
                    fadeCardViewTextViews(0.0f, 0)
                    animateTextContainer()
                    isEnterAnimating = true
                    cardRootView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            }
        })
    }

    private fun animateTextContainer() {
        ObjectAnimator.ofFloat(textContainer, View.ALPHA, textContainer.alpha, 1f).setDuration(MOVE_DURATION).start(runningAnimations)
        textContainer.translationY = targetSize.y + cardRootView.resources.getDimension(R.dimen.topbar_height)
        ObjectAnimator.ofFloat(textContainer, View.SCALE_X, 2f, 1f).setDuration(MOVE_DURATION).interpolate(scaleInterpolator).start(runningAnimations)
        ObjectAnimator.ofFloat(textContainer, View.SCALE_Y, 2f, 1f).setDuration(MOVE_DURATION).interpolate(scaleInterpolator).start(runningAnimations)

        val textBlur = textContainer.textBlur
        ValueAnimator.ofFloat(TARGET_TEXT_BLUR_RADIUS, 0f).setDuration((MOVE_DURATION).toLong())
                .onUpdate { value ->
                    textBlur.blurRadius = value as Float
                    textBlur.updateForMilliSeconds(16)
                }.onEnd {
                    textBlur.viewBehind?.visibility = View.VISIBLE
                    textBlur.disable()
                }.interpolate(LinearInterpolator()).start(runningAnimations)
    }

    fun animateCardOut() {
        cancelAllRunningAnimations()
        animateCardPosition(0f)
        animateCardSize(originSize)
        animateCardCornerRadius(cardRootView.resources.getDimension(R.dimen.card_view_corner_radius))
        fadeCardViewTextViews(1.0f, MOVE_DURATION - FADE_TEXT_DURATION)
        scaleBackgroundView(1f, 0.3f, scaleInterpolator, -0.6f)
        ObjectAnimator.ofFloat(textContainer, View.ALPHA, textContainer.alpha, 0f).setDuration((MOVE_DURATION * 0.4f).toLong()).start(runningAnimations)
    }

    fun cancelAllRunningAnimations() {
        for (anim in runningAnimations) {
            anim.removeAllListeners()
            anim.cancel()
        }
        runningAnimations.clear()
    }

    fun fadeInBlur(blurView: BlurBehindLayout, blurDimmer: View, textBlur: BlurBehindLayout) {
        ValueAnimator.ofFloat(0f, TARGET_BLUR_RADIUS).setDuration(MOVE_DURATION - FADE_BARS_DURATION)
                .interpolate(scaleInterpolator).delay(FADE_BARS_DURATION).onUpdate { value ->
                    blurView.blurRadius = value as Float
                }.onEnd { isEnterAnimating = false }.start(runningAnimations)

        ObjectAnimator.ofFloat(blurDimmer, View.ALPHA, 0f, 1f).setDuration((MOVE_DURATION * 1.5f).toLong())
                .interpolate(moveInterpolator).delay(FADE_BARS_DURATION).start(runningAnimations)
    }

    fun fadeOutFullscreenBlur(blurView: BlurBehindLayout, blurDimmer: View) {
        blurView.enable()
        blurView.updateForMilliSeconds(MOVE_DURATION)
        val blurMultiplier = if (isEnterAnimating) 1f else 2f
        ValueAnimator.ofFloat(blurView.blurRadius * blurMultiplier, 0f).setDuration((MOVE_DURATION * 0.9f).toLong())
                .interpolate(LinearInterpolator()).onUpdate { value ->
                    blurView.blurRadius = value as Float
                }.onEnd {
                    blurView.disable()
                }.start(runningAnimations)

        ObjectAnimator.ofFloat(blurDimmer, View.ALPHA, blurDimmer.alpha, 0f).setDuration((MOVE_DURATION - FADE_BARS_DURATION).toLong())
                .interpolate(moveInterpolator).start(runningAnimations)
    }

    fun fadeOutTopAndBottomBlurViews(activity: Activity?) {
        val appBarDimmer = activity?.findViewById<View>(R.id.appBarFullDimmer)
        val navBarDimmer = activity?.findViewById<View>(R.id.navigationBarFullDimmer)
        appBarDimmer?.visibility = View.VISIBLE
        navBarDimmer?.visibility = View.VISIBLE
        val appBarBlur = activity?.findViewById<BlurBehindLayout>(R.id.appBarBlurLayout)
        val navBarBlur = activity?.findViewById<BlurBehindLayout>(R.id.navigationBarBlurLayout)

        ObjectAnimator.ofFloat(appBarDimmer, View.ALPHA, 0f, 1f).setDuration(FADE_BARS_DURATION).start(runningAnimations)
        ObjectAnimator.ofFloat(navBarDimmer, View.ALPHA, 0f, 1f).setDuration(FADE_BARS_DURATION).onEnd {
            appBarBlur?.disable()
            navBarBlur?.disable()
        }.start(runningAnimations)
    }

    fun fadeInTopAndBottomBlurViews(activity: Activity?, onExitAnimationComplete: () -> Unit) {
        val appBarDimmer = activity?.findViewById<View>(R.id.appBarFullDimmer)!!
        val navBarDimmer = activity?.findViewById<View>(R.id.navigationBarFullDimmer)
        val appBarBlur = activity?.findViewById<BlurBehindLayout>(R.id.appBarBlurLayout)
        val navBarBlur = activity?.findViewById<BlurBehindLayout>(R.id.navigationBarBlurLayout)
        appBarBlur?.enable()
        navBarBlur?.enable()

        ObjectAnimator.ofFloat(appBarDimmer, View.ALPHA, appBarDimmer.alpha, 0f).setDuration(MOVE_DURATION).onStart {
            appBarBlur?.updateForMilliSeconds(MOVE_DURATION / 2)
            navBarBlur?.updateForMilliSeconds(MOVE_DURATION / 2)
        }.start(runningAnimations)
        ObjectAnimator.ofFloat(navBarDimmer, View.ALPHA, appBarDimmer.alpha, 0f).setDuration(MOVE_DURATION).onEnd {
            navBarDimmer?.visibility = View.GONE
            appBarDimmer?.visibility = View.GONE
            onExitAnimationComplete.invoke()
        }.start(runningAnimations)
    }

    private fun fadeCardViewTextViews(toAlpha: Float, delay: Long) {
        ObjectAnimator.ofFloat(cardView.heading, View.ALPHA, cardView.heading.alpha, toAlpha).setDuration(FADE_TEXT_DURATION).delay(delay).start(runningAnimations)
        if (cardView.subHeading != null)
            ObjectAnimator.ofFloat(cardView.subHeading, View.ALPHA, cardView.subHeading.alpha, toAlpha).setDuration(FADE_TEXT_DURATION).delay(delay).start(runningAnimations)

    }

    private fun animateCardPosition(toPosition: Float) {

        val pm = PathMeasure(movePath, false)
        ValueAnimator.ofFloat(currentPathMoveProgress, toPosition).setDuration(MOVE_DURATION).interpolate(moveInterpolator).onUpdate { anim ->
            currentPathMoveProgress = anim as Float
            pm.getPosTan(pm.getLength() * currentPathMoveProgress, cardViewCenterPosition, null);

        }.start(runningAnimations)
    }

    private fun createMovePath(): Path {
        val movePath = Path()
        val cardViewPos = Rect()
        val centerX = targetSize.x / 2f
        val centerY = targetSize.y / 2f + cardRootView.resources.getDimension(R.dimen.topbar_height)
        cardRootView.getHitRect(cardViewPos)
        movePath.moveTo(cardViewPos.exactCenterX(), cardViewPos.exactCenterY())
        movePath.cubicTo(centerX + (centerX - cardViewPos.exactCenterX()) * 0.5f, cardViewPos.exactCenterY() - (cardViewPos.exactCenterY() - centerY) / 4,
                centerX, cardViewPos.exactCenterY() + (centerY - cardViewPos.exactCenterY()) / 2f,
                centerX, centerY)
        return movePath
    }

    private fun animateCardCornerRadius(toRadius: Float) {
        ValueAnimator.ofFloat(cardView.radius, toRadius).setDuration(MOVE_DURATION / 2).delay(MOVE_DURATION / 2)
                .onUpdate { value ->
                    val cardRadius = value as Float
                    cardView.radius = cardRadius
                }.start(runningAnimations)
    }

    private fun animateCardSize(targetSize: PointF) {
        ValueAnimator.ofObject(PointFEvaluator(), PointF(cardRootView.width.toFloat(), cardRootView.height.toFloat()), targetSize)
                .setDuration(MOVE_DURATION).interpolate(moveInterpolator).onUpdate { value ->
                    val size = value as PointF
                    contrainSet.clone(cardRootView)
                    contrainSet.setDimensionRatio(cardView.id, "1:" + (size.y / size.x))
                    contrainSet.applyTo(cardRootView)

                    var params = cardRootView.layoutParams as FrameLayout.LayoutParams
                    params.leftMargin = (cardViewCenterPosition[0] - size.x / 2).toInt()
                    params.topMargin = (cardViewCenterPosition[1] - size.y / 2).toInt()
                    params.width = size.x.toInt()
                    params.height = size.y.toInt()
                    cardRootView.layoutParams = params
                }.start(runningAnimations)
    }

    private fun scaleBackgroundView(toSize: Float, pivotY: Float, scaleInterpolator: PathInterpolator, rotateAmount: Float) {
        var easedScale = (backgroundView.scaleX - BACKGROUND_VIEWS_SCALED_DOWN_SIZE) * 200f
        var easedScaleOffset = 0f
        var disableFlipAnimation = false
        frameRateCounter.timeStep()

        if (!isEnterAnimating) {
            backgroundView.pivotY = backgroundView.height * pivotY
        } else {
            disableFlipAnimation = true
            ObjectAnimator.ofFloat(backgroundView, View.ROTATION_X, backgroundView.rotationX, 0f).setDuration(MOVE_DURATION - FADE_BARS_DURATION)
                    .delay(FADE_BARS_DURATION).interpolate(moveInterpolator).start(runningAnimations)
        }
        ValueAnimator.ofFloat(backgroundView.scaleX, toSize).setDuration(MOVE_DURATION - FADE_BARS_DURATION)
                .delay(FADE_BARS_DURATION).interpolate(scaleInterpolator).onUpdate { anim ->
                    val scale = anim as Float
                    backgroundView.scaleX = scale
                    backgroundView.scaleY = scale

                    if (!disableFlipAnimation) {
                        //Little trick to give the impression ov some air resistance making the view flip slightly :)
                        val targetScaleForEasedRotaion = (scale - BACKGROUND_VIEWS_SCALED_DOWN_SIZE) * 200f
                        val time = frameRateCounter.timeStep()
                        val easeAmount = ((targetScaleForEasedRotaion - easedScale) * time) * 15f
                        easedScaleOffset += (easeAmount - easedScaleOffset) * time * 20f
                        backgroundView.rotationX = easedScaleOffset * rotateAmount
                        easedScale += easeAmount
                    }

                }.start(runningAnimations)
    }
}