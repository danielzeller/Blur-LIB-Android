package no.danielzeller.blurbehind

import android.arch.lifecycle.ViewModel
import android.content.Context
import com.squareup.picasso.LruCache
import com.squareup.picasso.Picasso

class UnsplashViewModel : ViewModel() {
    lateinit var picasso: Picasso

    fun createPicasso(context: Context) {
        picasso = Picasso.Builder(context.applicationContext).memoryCache(picassoCache).build()
    }

    val picassoCache = LruCache(100000000)

}