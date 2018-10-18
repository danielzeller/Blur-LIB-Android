package no.danielzeller.blurbehind.fragments


import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.view.animation.PathInterpolator
import kotlinx.android.synthetic.main.fragment_dialog.view.*
import no.danielzeller.blurbehind.R
import no.danielzeller.blurbehind.animation.*
import no.danielzeller.blurbehind.extensions.delay
import no.danielzeller.blurbehind.extensions.interpolate
import no.danielzeller.blurbehind.extensions.onEnd
import no.danielzeller.blurbehind.extensions.onUpdate
import no.danielzeller.blurbehindlib.BlurBehindLayout

const val SCALED_DOWN_SIZE = 0.7f
const val ANIM_DURATION = 400L

class DialogFragment : Fragment(), View.OnTouchListener {

    private lateinit var blurViewContent: View
    private lateinit var blurView: BlurBehindLayout
    private val frameRateCounter = FrameRateCounter()
    private var isExitAnimating = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_dialog, container, false)
        rootView.setOnTouchListener(this)
        setupBlurView(rootView)
        enterAnimate(rootView)
        return rootView
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        exitAnimateAndRemove()
        return false
    }

    private fun enterAnimate(rootView: View) {
        blurViewContent = rootView.blurViewFirstChild
        blurViewContent.scaleX = SCALED_DOWN_SIZE
        blurViewContent.scaleY = SCALED_DOWN_SIZE
        rootView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {

                scaleBackgroundView(1f, 0.3f, scaleInterpolator, -1.2f)
                rootView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                ObjectAnimator.ofFloat(blurView, View.ALPHA, 0f, 1f).setDuration(ANIM_DURATION / 2).delay(50).start()
                blurView.updateForMilliSeconds(ANIM_DURATION)
            }
        })
    }

    private fun setupBlurView(rootView: View) {
        blurView = rootView.dialogBlurView
        blurView.viewBehind = activity?.findViewById(R.id.viewToBlur)
        blurView.alpha = 0f
    }

    fun exitAnimateAndRemove() {
        if (!isExitAnimating) {
            isExitAnimating = true
            blurView.updateForMilliSeconds(ANIM_DURATION)
            scaleBackgroundView(SCALED_DOWN_SIZE, 0.5f, scaleInterpolator, -1.5f)
            ObjectAnimator.ofFloat(blurView, View.ALPHA, 1f, 0f).setDuration(ANIM_DURATION).onEnd {
                fragmentManager?.beginTransaction()?.remove(this)?.commit()
            }.start()
        }
    }

    private fun scaleBackgroundView(toSize: Float, pivotY: Float, scaleInterpolator: PathInterpolator, rotateAmount: Float) {

        var easedScale = (blurViewContent.scaleX - SCALED_DOWN_SIZE) * 200f
        var easedScaleOffset = 0f
        frameRateCounter.timeStep()
        blurViewContent.pivotY = blurViewContent.height * pivotY

        ValueAnimator.ofFloat(blurViewContent.scaleX, toSize).setDuration(ANIM_DURATION)
                .interpolate(scaleInterpolator).onUpdate { anim ->
                    val scale = anim as Float
                    blurViewContent.scaleX = scale
                    blurViewContent.scaleY = scale

                    //Little trick to give the impression ov some air resistance making the view flip slightly :)
                    val targetScaleForEasedRotation = (scale - SCALED_DOWN_SIZE) * 200f
                    val time = frameRateCounter.timeStep()
                    val easeAmount = ((targetScaleForEasedRotation - easedScale) * time) * 20f
                    easedScaleOffset += (easeAmount - easedScaleOffset) * time * 30f
                    blurViewContent.rotationX = easedScaleOffset * rotateAmount
                    easedScale += easeAmount


                }.start()
    }
}
