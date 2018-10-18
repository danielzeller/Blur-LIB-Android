package no.danielzeller.blurbehind

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.activity_main.*
import no.danielzeller.blurbehind.fragments.DetailsFragment
import no.danielzeller.blurbehind.fragments.DialogFragment
import no.danielzeller.blurbehind.fragments.ORIGIN_VIEW_TAG
import no.danielzeller.blurbehind.model.UnsplashItem
import android.content.Intent
import android.net.Uri


const val DETAILS_FRAGMENT_TAG = "details_fragment_tag"
const val DIALOG_FRAGMENT_TAG = "dialog_fragment_tag"
private const val UNSPLASH_RANDOM_URL = "https://source.unsplash.com/random/960x540?"
private const val CARDS_COUNT = 15

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupBlurBiews()
        setupRecyclerView()
        appBarDimmer.layoutParams = getStatusBarHeightParams()
    }

    private fun setupBlurBiews() {
        appBarBlurLayout.viewBehind = viewToBlur
        navigationBarBlurLayout.viewBehind = viewToBlur
    }

    private fun setupRecyclerView() {
        val gridLayoutManager = GridLayoutManager(this, 2)

        val viewModel = ViewModelProviders.of(this).get(UnsplashViewModel::class.java)
        viewModel.createPicasso(this)
        val unsplashGridAdapter = UnsplashGridAdapter(createUnsplashItems(), viewModel, listOf<Any>(cardClickedUnit, textClickedAction1, textClickedAction2, textClickedAction3))

        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(p0: Int): Int {
                if (unsplashGridAdapter.getItemViewType(p0) == R.layout.card4) {
                    return 1
                }
                return 2
            }
        }
        recyclerView.addItemDecoration(GridSpaces(resources.getDimension(R.dimen.grid_item_horizontal_space).toInt(), resources.getDimension(R.dimen.grid_top_padding).toInt(), resources.getDimension(R.dimen.grid_bottom_padding).toInt()))
        recyclerView.layoutManager = gridLayoutManager
        recyclerView.adapter = unsplashGridAdapter
    }

    private fun createUnsplashItems(): ArrayList<UnsplashItem> {
        val items = ArrayList<UnsplashItem>()

        val cardsLayouts = intArrayOf(R.layout.card2, R.layout.card3, R.layout.card4, R.layout.card4, R.layout.card1)
        val headings = resources.getStringArray(R.array.headings)
        val subHeadings = resources.getStringArray(R.array.sub_headings)
        val articleContent = resources.getStringArray(R.array.articles_content)
        var cardTypeIndex = 0
        for (i in 0 until CARDS_COUNT) {
            items.add(UnsplashItem(UNSPLASH_RANDOM_URL + i, headings[i], subHeadings[i], cardsLayouts[cardTypeIndex], articleContent[i], getClickUnit(cardsLayouts[cardTypeIndex])))

            cardTypeIndex += 1
            if (cardTypeIndex == cardsLayouts.size) cardTypeIndex = 0
        }
        return items
    }

    private fun getStatusBarHeightParams(): FrameLayout.LayoutParams {

        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        val margin = resources.getDimensionPixelSize(resourceId)
        val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.topMargin = margin
        return layoutParams
    }

    private val textClickedAction1: (() -> Unit) = {
        if (!isDialogVisible()) {
            val dialogfragment = DialogFragment()
            supportFragmentManager.beginTransaction().add(R.id.overlayFragmentContainer, dialogfragment, DIALOG_FRAGMENT_TAG).commitNow()
        }
    }

    private val textClickedAction2: (() -> Unit) = {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/danielzeller"))
        startActivity(browserIntent)
    }

    private val textClickedAction3: (() -> Unit) = {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://danielzeller.no"))
        startActivity(browserIntent)
    }

    private val cardClickedUnit: ((itemView: View, item: UnsplashItem) -> Unit) = { itemView: View, item: UnsplashItem ->
        if (!isDialogVisible()) {
            val detailsFragment = DetailsFragment.newInstance(itemView, item)
            supportFragmentManager.beginTransaction().add(R.id.overlayFragmentContainer, detailsFragment, DETAILS_FRAGMENT_TAG).commitNow()
            itemView.visibility = View.INVISIBLE
            itemView.tag = ORIGIN_VIEW_TAG
        }
    }
    private var texClickedIndex = 0

    private fun getClickUnit(clickType: Int): Int {
        if (clickType == R.layout.card1) {
            texClickedIndex += 1
            return texClickedIndex
        }
        return 0
    }

    private fun isDialogVisible() =
            supportFragmentManager.findFragmentByTag(DIALOG_FRAGMENT_TAG) != null


    override fun onBackPressed() {
        var exitApp = true
        var fragment = supportFragmentManager.findFragmentByTag(DIALOG_FRAGMENT_TAG)
        if (fragment != null) {
            (fragment as DialogFragment).exitAnimateAndRemove()
            exitApp = false
        }

        fragment = supportFragmentManager.findFragmentByTag(DETAILS_FRAGMENT_TAG)
        if (fragment != null) {
            val detailsFragment = fragment as DetailsFragment
            if (!detailsFragment.isExitAnimating) {
                detailsFragment.exitAnimateAndRemove()
                exitApp = false
            } else {
                detailsFragment.cancelAllRunningAnimations()
            }
        }

        if (exitApp) {
            super.onBackPressed()
        }
    }
}