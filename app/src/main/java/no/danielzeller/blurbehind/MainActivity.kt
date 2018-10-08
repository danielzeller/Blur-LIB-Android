package no.danielzeller.blurbehind

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup
import android.widget.FrameLayout
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.card2.*
import kotlinx.android.synthetic.main.card3.*
import kotlinx.android.synthetic.main.card4.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        topBarBlurLayout.viewBehind = viewToBlur
        navigationBarBlurLayout.viewBehind = viewToBlur
        appBarDimmer.layoutParams = getStatusBarHeightParams()
        val downloader = OkHttp3Downloader(this)
        Picasso.Builder(this).downloader(downloader).build().load("https://source.unsplash.com/random").into(imageView2);
        Picasso.Builder(this).downloader(downloader).build().load("https://source.unsplash.com/random").into(imageView3);
        Picasso.Builder(this).downloader(downloader).build().load("https://source.unsplash.com/random").into(imageView4);
        Picasso.Builder(this).downloader(downloader).build().load("https://source.unsplash.com/random").into(imageView5);
    }

    fun getStatusBarHeightParams(): FrameLayout.LayoutParams {

        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")

        val margin = resources.getDimensionPixelSize(resourceId)
        val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.topMargin = margin
        return layoutParams
    }
}
