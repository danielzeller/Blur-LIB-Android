package no.danielzeller.blurbehind

import android.animation.*
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.SystemClock
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.TextView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.card2.view.*
import kotlinx.android.synthetic.main.fragment_details.view.*
import kotlinx.android.synthetic.main.loader_view.view.*
import no.danielzeller.blurbehind.model.UnsplashItem
import no.danielzeller.blurbehindlib.BlurBehindLayout
import android.support.constraint.ConstraintSet
import android.view.animation.PathInterpolator
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.fragment_details.*
import no.danielzeller.blurbehind.extensions.onEnd
import no.danielzeller.blurbehindlib.UpdateMode


private const val CLICKED_VIEW_SCREEN_POSITION_KEY = "screen_pos"
private const val UNSPLASH_ITEM_KEY = "unsplash_item"
private const val FADE_BARS_DURATION = 100L
private const val MOVE_DURATION = 700L
private const val TARGET_BLUR_RADIUS = 30f
private const val BACKGROUND_VIEWS_SCALED_DOWN_SIZE = 0.9f

class DetailsFragment : Fragment() {

    private var clickedViewScreenPos = Rect()
    private lateinit var unsplashItem: UnsplashItem
    private lateinit var cardRootView: ConstraintLayout
    private lateinit var cardView: CardView
    private val set = ConstraintSet()
    private val cardViewCenterPosition = floatArrayOf(0f, 0f)
    private val moveInterpolator = PathInterpolator(.52f, 0f, .18f, 1f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val clickedViewScreenPosArray = it.getIntegerArrayList(CLICKED_VIEW_SCREEN_POSITION_KEY)
            unsplashItem = it.getSerializable(UNSPLASH_ITEM_KEY) as UnsplashItem
            clickedViewScreenPos.set(clickedViewScreenPosArray[0], clickedViewScreenPosArray[1], clickedViewScreenPosArray[2], clickedViewScreenPosArray[3])
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_details, container, false)

        val viewToBlur = activity?.findViewById<View>(R.id.viewToBlur) as ViewGroup
        rootView.fullscreenBlur.viewBehind = viewToBlur
        rootView.fullscreenBlur.updateForMilliSeconds(MOVE_DURATION)

        fadeInBlur(rootView.fullscreenBlur, rootView.fullscreenDimmer)
        fadeTopAndBottomBlurViews()
        setupCopyCardView(rootView as FrameLayout, inflater)
        animateCopyCard(container!!, viewToBlur.recyclerView)
        return rootView
    }

    var easedScale = 0f
    var easedScaleOffset = 0f
    private fun scaleBackgroundView(recyclerView: ViewGroup) {

        var scaleViewBehindAnim = ValueAnimator.ofFloat(1f, BACKGROUND_VIEWS_SCALED_DOWN_SIZE).setDuration(MOVE_DURATION - FADE_BARS_DURATION)
        scaleViewBehindAnim.addUpdateListener { animation ->
            val scale = animation.animatedValue as Float
            recyclerView.scaleX = scale
            recyclerView.scaleY = scale

        }
        scaleViewBehindAnim.interpolator = PathInterpolator(.24f, -0.01f, .13f, .99f)
        scaleViewBehindAnim.startDelay = FADE_BARS_DURATION
        scaleViewBehindAnim.start()

//        val dur1 = ((MOVE_DURATION - FADE_BARS_DURATION) * 0.2).toLong()
//        val rot1 = ObjectAnimator.ofFloat(recyclerView, View.ROTATION_X, 0f, 1f).setDuration(dur1)
//        rot1.startDelay = FADE_BARS_DURATION
//        rot1.interpolator = moveInterpolator
//        rot1.start()
//        val dur2 = ((MOVE_DURATION - FADE_BARS_DURATION) * 0.8).toLong()
//        val rot2 = ObjectAnimator.ofFloat(recyclerView, View.ROTATION_X, 1f, 0f).setDuration(dur2)
//        rot2.startDelay = FADE_BARS_DURATION + dur1
//        rot2.interpolator = moveInterpolator
//        rot2.start()
        fc.timeStep()
        var easeRotation = ValueAnimator.ofFloat(0f, 20f).setDuration(MOVE_DURATION-FADE_BARS_DURATION)
        easeRotation.addUpdateListener { animation ->
            val scale = animation.animatedValue as Float
            val time=fc.timeStep()
            val easeAmount = ((scale - easedScale) *  time) * 15f
            easedScaleOffset+=(easeAmount-easedScaleOffset)*time*20f
            recyclerView.rotationX = easedScaleOffset*1.3f
            easedScale += easeAmount


        }
        easeRotation.interpolator = PathInterpolator(.24f, -0.01f, .13f, .99f)
        easeRotation.startDelay = FADE_BARS_DURATION
        easeRotation.start()

    }

    val fc = FrameRateCounter()

    class FrameRateCounter {
        private var mLastTime: Long = 0

        fun timeStep(): Float {
            val time = SystemClock.uptimeMillis()
            val timeDelta = time - mLastTime
            val timeDeltaSeconds = if (mLastTime > 0.0f) timeDelta / 1000.0f else 0.0f
            mLastTime = time
            return Math.min(0.015f, timeDeltaSeconds)
        }
    }

    private fun animateCopyCard(container: ViewGroup, recyclerView: ViewGroup) {
        container.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (cardRootView.image.drawable != null) {
                    var targetWidth = container.width.toFloat()
                    var drawable = cardRootView.image.drawable
                    var targetHeight = Math.min(targetWidth, (drawable.intrinsicHeight.toFloat() / drawable.intrinsicWidth.toFloat()) * targetWidth)
                    var targetSize = PointF(targetWidth, targetHeight)
                    animatePosition(targetSize)
                    animateSize(targetSize)
                    animateCornerRadius()
                    container.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    scaleBackgroundView(recyclerView)
                }
            }
        })
    }


    private fun animatePosition(targetSize: PointF) {
        val movePath = Path()
        val cardViewPos = Rect()
        val centerX = targetSize.x / 2f
        val centerY = targetSize.y / 2f + fullscreenBlur.top
        cardRootView.getHitRect(cardViewPos)
        movePath.moveTo(cardViewPos.exactCenterX(), cardViewPos.exactCenterY())
        movePath.cubicTo(centerX + (centerX - cardViewPos.exactCenterX()) * 0.5f, cardViewPos.exactCenterY() - (cardViewPos.exactCenterY() - centerY) / 4, centerX, cardViewPos.exactCenterY() + (centerY - cardViewPos.exactCenterY()) / 2f, centerX, centerY)
        val pm = PathMeasure(movePath, false)
        val anim = ValueAnimator.ofFloat(0f, 1f).setDuration(MOVE_DURATION)
        anim.addUpdateListener { animation ->
            pm.getPosTan(pm.getLength() * animation.animatedValue as Float, cardViewCenterPosition, null);
        }
        anim.interpolator = moveInterpolator
        anim.start()
    }

    fun animateCornerRadius() {
        var cardRadius = ValueAnimator.ofFloat(cardView.radius, 0f).setDuration(MOVE_DURATION / 2)
        cardRadius.addUpdateListener { animation ->
            val cardRadius = animation.animatedValue as Float
            cardView.radius = cardRadius
        }
        cardRadius.startDelay = MOVE_DURATION / 2
        cardRadius.start()
    }

    fun animateSize(targetSize: PointF) {
        var sizeAnim = ValueAnimator.ofObject(PointFEvaluator(), PointF(cardRootView.width.toFloat(), cardRootView.height.toFloat()), targetSize).setDuration(MOVE_DURATION)
        sizeAnim.addUpdateListener { animation ->
            val size = animation.animatedValue as PointF
            set.clone(cardRootView)
            set.setDimensionRatio(cardView.id, "1:" + (size.y / size.x))
            set.applyTo(cardRootView)

            var params = cardRootView.layoutParams as FrameLayout.LayoutParams
            params.leftMargin = (cardViewCenterPosition[0] - size.x / 2).toInt()
            params.topMargin = (cardViewCenterPosition[1] - size.y / 2).toInt()
            params.width = size.x.toInt()
            params.height = size.y.toInt()
            cardRootView.layoutParams = params
        }
        sizeAnim.interpolator = moveInterpolator
        sizeAnim.start()
    }

    private fun setupCopyCardView(rootView: FrameLayout, inflater: LayoutInflater) {
        cardRootView = inflater.inflate(unsplashItem.layoutID, rootView, false) as ConstraintLayout
        val layoutParams = FrameLayout.LayoutParams(clickedViewScreenPos.right - clickedViewScreenPos.left, clickedViewScreenPos.bottom - clickedViewScreenPos.top)
        layoutParams.leftMargin = clickedViewScreenPos.left
        layoutParams.topMargin = clickedViewScreenPos.top
        rootView.addView(cardRootView, layoutParams)

        Picasso.get().load(unsplashItem.imageUrl).fit().centerInside().into(cardRootView.image)
        cardRootView.progressView.visibility = View.GONE
        cardRootView.heading.text = unsplashItem.heading
        cardRootView.findViewById<TextView>(R.id.subHeading)?.text = unsplashItem.subHeading
        cardView = cardRootView.getChildAt(0) as CardView
        ObjectAnimator.ofFloat(cardView.heading, View.ALPHA, 1f, 0f).setDuration(300).start()
        if (cardView.subHeading != null)
            ObjectAnimator.ofFloat(cardView.subHeading, View.ALPHA, 1f, 0f).setDuration(300).start()

    }

    private fun fadeTopAndBottomBlurViews() {
        val appBarDimmer = activity?.findViewById<View>(R.id.appBarFullDimmer)
        val navBarDimmer = activity?.findViewById<View>(R.id.navigationBarFullDimmer)
        appBarDimmer?.visibility = View.VISIBLE
        navBarDimmer?.visibility = View.VISIBLE
        val appBarBlur = activity?.findViewById<BlurBehindLayout>(R.id.appBarBlurLayout)
        val navBarBlur = activity?.findViewById<BlurBehindLayout>(R.id.navigationBarBlurLayout)
        appBarBlur?.updateMode = UpdateMode.MANUALLY
        navBarBlur?.updateMode = UpdateMode.MANUALLY

        ObjectAnimator.ofFloat(appBarDimmer, View.ALPHA, 0f, 1f).setDuration(FADE_BARS_DURATION).start()
        val fadeAnim2 = ObjectAnimator.ofFloat(navBarDimmer, View.ALPHA, 0f, 1f).setDuration(FADE_BARS_DURATION)
        fadeAnim2.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                appBarBlur?.disable()
                navBarBlur?.disable()
            }
        })
        fadeAnim2.start()
    }

    private fun fadeInBlur(blurView: BlurBehindLayout, blurDimmer: View) {
        var radiusAnim = ValueAnimator.ofFloat(0f, TARGET_BLUR_RADIUS).setDuration(MOVE_DURATION - FADE_BARS_DURATION)
        radiusAnim.addUpdateListener { animation ->
            blurView.blurRadius = animation.animatedValue as Float
        }
        radiusAnim.interpolator = PathInterpolator(.24f, -0.01f, .13f, .99f)
        radiusAnim.startDelay = FADE_BARS_DURATION
        radiusAnim.start()
        var alphaDimmer = ObjectAnimator.ofFloat(blurDimmer, View.ALPHA, 0f, 1f).setDuration((MOVE_DURATION * 1.5f).toLong())
        alphaDimmer.interpolator = moveInterpolator
        alphaDimmer.startDelay = FADE_BARS_DURATION
        alphaDimmer.start()

    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param clickedView ClickedView.
         * @return A new instance of fragment DetailsFragment.
         */
        @JvmStatic
        fun newInstance(clickedView: View, unsplashItem: UnsplashItem) =
                DetailsFragment().apply {
                    val rect = Rect()
                    clickedView.getHitRect(rect)
                    arguments = Bundle().apply {
                        putIntegerArrayList(CLICKED_VIEW_SCREEN_POSITION_KEY, arrayListOf(rect.left, rect.top, rect.right, rect.bottom))
                        putSerializable(UNSPLASH_ITEM_KEY, unsplashItem)
                    }
                }
    }
}
