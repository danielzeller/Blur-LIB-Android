package no.danielzeller.blurbehind

import android.graphics.*
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.card2.view.*
import kotlinx.android.synthetic.main.fragment_details.view.*
import kotlinx.android.synthetic.main.loader_view.view.*
import no.danielzeller.blurbehind.model.UnsplashItem
import kotlinx.android.synthetic.main.activity_main.view.*
import no.danielzeller.blurbehind.animation.CardTransitionHelper
import no.danielzeller.blurbehind.animation.MOVE_DURATION


const val ORIGIN_VIEW_TAG = "origin_view_tag"
private const val ORIGIN_VIEW_SCREEN_POSITION_KEY = "screen_pos"
private const val UNSPLASH_ITEM_KEY = "unsplash_item"

class DetailsFragment : Fragment() {

    var isExitAnimating = false
    private lateinit var unsplashItem: UnsplashItem

    private var originViewScreenPos = Rect()
    private lateinit var cardTransitionHelper: CardTransitionHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val originViewScreenPosArray = it.getIntegerArrayList(ORIGIN_VIEW_SCREEN_POSITION_KEY)
            unsplashItem = it.getSerializable(UNSPLASH_ITEM_KEY) as UnsplashItem
            originViewScreenPos.set(originViewScreenPosArray[0], originViewScreenPosArray[1], originViewScreenPosArray[2], originViewScreenPosArray[3])
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_details, container, false)
        val viewToBlur = activity?.findViewById<View>(R.id.viewToBlur) as ViewGroup

        val cardViewRoot = createCardView(rootView as FrameLayout, inflater)
        setupFullscreenBlurView(rootView, viewToBlur)
        cardTransitionHelper = CardTransitionHelper(cardViewRoot, viewToBlur.recyclerView)
        cardTransitionHelper.animateCardIn()
        cardTransitionHelper.fadeInFullscreenBlur(rootView.fullscreenBlur, rootView.fullscreenDimmer)
        cardTransitionHelper.fadeOutTopAndBottomBlurViews(activity)
        return rootView
    }

    private fun createCardView(rootView: FrameLayout, inflater: LayoutInflater): ConstraintLayout {
        val cardRootView = inflater.inflate(unsplashItem.layoutID, rootView, false) as ConstraintLayout
        val layoutParams = FrameLayout.LayoutParams(originViewScreenPos.right - originViewScreenPos.left, originViewScreenPos.bottom - originViewScreenPos.top)
        layoutParams.leftMargin = originViewScreenPos.left
        layoutParams.topMargin = originViewScreenPos.top
        rootView.addView(cardRootView, layoutParams)
        cardRootView.progressView.visibility = View.GONE
        cardRootView.heading.text = unsplashItem.heading
        cardRootView.findViewById<TextView>(R.id.subHeading)?.text = unsplashItem.subHeading

        Picasso.get().load(unsplashItem.imageUrl).fit().centerInside().into(cardRootView.image)

        return cardRootView
    }

    private fun setupFullscreenBlurView(rootView: View, viewToBlur: ViewGroup) {
        rootView.fullscreenBlur.viewBehind = viewToBlur
        rootView.fullscreenBlur.updateForMilliSeconds(MOVE_DURATION)
    }

    private fun onExitAnimationComplete() {
        val viewToBlur = activity?.findViewById<View>(R.id.viewToBlur) as ViewGroup
        val originView = viewToBlur.findViewWithTag<View>(ORIGIN_VIEW_TAG)
        originView.visibility = View.VISIBLE
        originView.tag = null
        fragmentManager?.beginTransaction()?.remove(this)?.commit()
    }

    fun exitAnimateAndRemove() {
        cardTransitionHelper.animateCardOut()
        cardTransitionHelper.fadeOutFullscreenBlur(view!!.fullscreenBlur, view!!.fullscreenDimmer)
        cardTransitionHelper.fadeInTopAndBottomBlurViews(activity, { onExitAnimationComplete() })
        isExitAnimating = true
    }

    fun cancelAllRunnungAnimations() {
        cardTransitionHelper.cancelAllRunningAnimations()
    }

    companion object {
        /**
         * @param clickedView ClickedView.
         * @return A new instance of fragment DetailsFragment.
         */
        @JvmStatic
        fun newInstance(clickedView: View, unsplashItem: UnsplashItem) =
                DetailsFragment().apply {
                    val rect = Rect()
                    clickedView.getHitRect(rect)
                    arguments = Bundle().apply {
                        putIntegerArrayList(ORIGIN_VIEW_SCREEN_POSITION_KEY, arrayListOf(rect.left, rect.top, rect.right, rect.bottom))
                        putSerializable(UNSPLASH_ITEM_KEY, unsplashItem)
                    }
                }
    }
}
