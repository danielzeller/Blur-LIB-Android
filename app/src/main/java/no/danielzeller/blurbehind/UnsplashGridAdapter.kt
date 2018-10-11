package no.danielzeller.blurbehind

import android.graphics.Bitmap
import android.graphics.drawable.Animatable
import android.support.v4.app.FragmentManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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
            if (viewHolder.progressBar.visibility == View.GONE) {
                val detailsFragment = DetailsFragment.newInstance(viewHolder.itemView, item)
                supportFragmentManager.beginTransaction().add(R.id.overlayFragmentContainer, detailsFragment, DETAILS_FRAGMENT_TAG).commitNow()
                viewHolder.itemView.visibility = View.INVISIBLE
                viewHolder.itemView.tag = ORIGIN_VIEW_TAG
            }
        }
    }

    fun setupImageView(viewHolder: CardViewHolder, item: UnsplashItem) {
        viewHolder.progressBar.visibility = View.VISIBLE
        (viewHolder.progressBarImage.drawable as Animatable).start()
        var loaderRef = WeakReference<View>(viewHolder.progressBar)
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