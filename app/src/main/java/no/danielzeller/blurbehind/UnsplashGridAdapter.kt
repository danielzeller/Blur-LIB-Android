package no.danielzeller.blurbehind

import android.graphics.Bitmap
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.support.v4.app.FragmentManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.card2.view.*
import kotlinx.android.synthetic.main.loader_view.view.*
import no.danielzeller.blurbehind.model.UnsplashItem
import java.lang.Exception
import java.lang.ref.WeakReference


class UnsplashGridAdapter(val items: List<UnsplashItem>, val supportFragmentManager: FragmentManager) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val picasso: Picasso = Picasso.get()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        if (viewType == R.layout.card4) {
            return CardViewHolder(v)
        }
        return CardViewHolder2(v)
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].layoutID
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        var viewHolder: CardViewHolder;
        if (holder.getItemViewType() == R.layout.card4) {
            viewHolder = holder as CardViewHolder;
        } else {
            viewHolder = holder as CardViewHolder2;
            viewHolder.subHeading.text = item.subHeading
        }
        viewHolder.heading.text = item.heading

        setupImageView(viewHolder, item)
        setupOnClickListener(viewHolder, item)
    }

    private fun setupOnClickListener(viewHolder: CardViewHolder, item: UnsplashItem) {
        viewHolder.itemView.setOnClickListener {
            //Normally we would have to deal with the progressbar in the animation but since this
            //is just a quick demo, we disable the onClick until the image is loaded.
            if (viewHolder.progressBar.visibility == View.GONE) {
                val detailsFragment = DetailsFragment.newInstance(viewHolder.itemView, item)
                supportFragmentManager.beginTransaction().add(R.id.overlayFragmentContainer, detailsFragment).commitNow()
                viewHolder.itemView.visibility = View.INVISIBLE
            }
        }
    }

    fun setupImageView(viewHolder: CardViewHolder, item: UnsplashItem) {
        viewHolder.progressBar.visibility = View.VISIBLE
        (viewHolder.progressBarImage.drawable as Animatable).start()
        var loaderRef = WeakReference<View>(viewHolder.progressBar)
//        Glide.with(viewHolder.image).
//                load(item.imageUrl).into(viewHolder.image)


//        applyDefaultRequestOptions(RequestOptions().format(DecodeFormat.PREFER_ARGB_8888)).
//        listener(object : RequestListener<Drawable> {
//            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
//                loaderRef.get()?.visibility = View.GONE
//                return true
//            }
//            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
//                loaderRef.get()?.visibility = View.GONE
//                return true
//            }
//        })

        picasso.load(item.imageUrl).fit().centerInside().config(Bitmap.Config.HARDWARE).into(viewHolder.image, object : Callback {
            override fun onSuccess() {
                loaderRef.get()?.visibility = View.GONE
            }

            override fun onError(e: Exception?) {
                loaderRef.get()?.visibility = View.GONE
            }
        })
    }

    inner class CardViewHolder2 : CardViewHolder {
        val subHeading: TextView

        constructor(view: View) : super(view) {
            subHeading = view.subHeading
        }
    }

    open inner class CardViewHolder : RecyclerView.ViewHolder {
        val image: ImageView
        val heading: TextView
        val progressBar: View
        val progressBarImage: ImageView

        constructor(view: View) : super(view) {
            image = view.image
            heading = view.heading
            progressBar = view.progressView
            progressBarImage = view.loader
        }
    }
}