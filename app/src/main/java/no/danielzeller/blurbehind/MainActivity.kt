package no.danielzeller.blurbehind

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.view.ViewGroup
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.activity_main.*
import no.danielzeller.blurbehind.model.UnsplashItem

const val DETAILS_FRAGMENT_TAG = "details_fragment_tag"
private const val UNSPLASH_RANDOM_URL = "https://source.unsplash.com/random/"
private const val CARDS_COUNT = 12

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
        val unsplashGridAdapter = UnsplashGridAdapter(createUnsplashItems(), supportFragmentManager, viewModel)

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

        val cardsLayouts = intArrayOf(R.layout.card2, R.layout.card3, R.layout.card4, R.layout.card4)
        val headings = resources.getStringArray(R.array.headings)
        val subHeadings = resources.getStringArray(R.array.sub_headings)
        val articleContent = resources.getStringArray(R.array.articles_content)
        var cardTypeIndex = 0
        val screenWidth = resources.displayMetrics.widthPixels
        for (i in 0 until CARDS_COUNT) {
            items.add(UnsplashItem(UNSPLASH_RANDOM_URL + screenWidth + "x" + (screenWidth * 3f / 4f) + "?" + i, headings[i], subHeadings[i], cardsLayouts[cardTypeIndex], articleContent[0]))

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

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentByTag(DETAILS_FRAGMENT_TAG)
        if (fragment != null) {
            val detailsFragment = fragment as DetailsFragment
            if (!detailsFragment.isExitAnimating) {
                detailsFragment.exitAnimateAndRemove()
            } else {
                detailsFragment.cancelAllRunningAnimations()
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }
}