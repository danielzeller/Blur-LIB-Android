package no.danielzeller.blurbehind

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.view.ViewGroup
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.activity_main.*
import no.danielzeller.blurbehind.model.UnsplashItem

class MainActivity : AppCompatActivity() {

    private val cardsLayouts = intArrayOf(R.layout.card2, R.layout.card3, R.layout.card4, R.layout.card4)
    private val UNSPLASH_RANDOM_URL = "https://source.unsplash.com/random?"
    private val CARDS_COUNT = 12

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupBlurBiews()
        setupRecyclerView()
        appBarDimmer.layoutParams = getStatusBarHeightParams()
    }

    fun setupBlurBiews() {
        topBarBlurLayout.viewBehind = viewToBlur
        navigationBarBlurLayout.viewBehind = viewToBlur
    }

    private fun setupRecyclerView() {
        val gridLayoutManager = GridLayoutManager(this, 2)
        val unsplashGridAdapter = UnsplashGridAdapter(createItems())

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

    private fun createItems(): ArrayList<UnsplashItem> {
        var items = ArrayList<UnsplashItem>()
        val headings = resources.getStringArray(R.array.headings)
        var cardTypeIndex = 0

        for (i in 0 until CARDS_COUNT) {
            items.add(UnsplashItem(UNSPLASH_RANDOM_URL + i, headings[i], "RANDOM IMAGE FROM UNSPLASH", cardsLayouts[cardTypeIndex]))

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
}