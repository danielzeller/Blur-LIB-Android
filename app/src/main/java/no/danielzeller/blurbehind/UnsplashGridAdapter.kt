package no.danielzeller.blurbehind

import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.squareup.picasso.Callback
import kotlinx.android.synthetic.main.card2.view.*
import no.danielzeller.blurbehind.animation.CLIP_ANIM_DURATION
import no.danielzeller.blurbehind.animation.LoaderImageView
import no.danielzeller.blurbehind.animation.scaleProgressBarInterpolator
import no.danielzeller.blurbehind.extensions.interpolate
import no.danielzeller.blurbehind.extensions.onUpdate
import no.danielzeller.blurbehind.model.UnsplashItem
import java.lang.Exception
import java.lang.ref.WeakReference


class UnsplashGridAdapter(val items: List<UnsplashItem>, private val viewModel: UnsplashViewModel, private val clickUnits: List<Any>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(viewType, parent, false)

        if (viewType == R.layout.card1) {
            return TextOnlyViewHolder(v)
        }
        return CardViewHolder(v)
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].layoutID
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        val viewHolder = holder as TextOnlyViewHolder

        viewHolder.subHeading.text = item.subHeading
        viewHolder.heading.text = item.heading

        if (holder is CardViewHolder) {
            setupImageView(holder, item)
            setupCardOnClickListener(holder, item)
        } else {
            setupTextOnClickListener(holder, item)
        }
    }

    private fun setupCardOnClickListener(viewHolder: CardViewHolder, item: UnsplashItem) {
        viewHolder.itemView.setOnClickListener {
            if (!viewHolder.image.isLoaderVisible) {
                (clickUnits[0] as ((itemView: View, item: UnsplashItem) -> Unit)).invoke(viewHolder.itemView, item)
            }
        }
    }

    private fun setupTextOnClickListener(viewHolder: TextOnlyViewHolder, item: UnsplashItem) {
        viewHolder.itemView.setOnClickListener {
            (clickUnits[item.clickUnitIndex] as (() -> Unit)).invoke()
        }
    }

    private fun setupImageView(viewHolder: CardViewHolder, item: UnsplashItem) {

        val bitmap = viewModel.picassoCache.get(item.imageUrl + "\n")
        viewHolder.cancelAnimations()
        if (bitmap == null) {
            loadImage(viewHolder, item)
        } else {
            viewHolder.image.setImageBitmap(bitmap)
            viewHolder.image.isLoaderVisible = false
        }
    }

    private fun loadImage(viewHolder: CardViewHolder, item: UnsplashItem) {

        viewHolder.image.isLoaderVisible = true
        viewHolder.image.cancelIntroAnim()
        viewHolder.setUpForTextAnim()

        val viewHolderRef = WeakReference<CardViewHolder>(viewHolder)
        viewModel.picasso.load(item.imageUrl).config(Bitmap.Config.HARDWARE).into(viewHolder.image, createOnImageLoadFinishedCallback(viewHolderRef))
    }

    private fun createOnImageLoadFinishedCallback(viewHolderRef: WeakReference<CardViewHolder>): Callback {
        return object : Callback {
            override fun onSuccess() {
                if (viewHolderRef.get() != null) {
                    val viewHolder = viewHolderRef.get()!!

                    viewHolder.image.introAnimate()

                    val translateText = ValueAnimator.ofFloat(viewHolder.image.width.toFloat() / 3f, 0f).setDuration(CLIP_ANIM_DURATION).interpolate(scaleProgressBarInterpolator).onUpdate { value ->
                        viewHolder.heading.translationY = value as Float
                        viewHolder.subHeading.translationY = value
                    }
                    translateText.start()
                    viewHolder.translateTextAnim = translateText
                }
            }

            override fun onError(e: Exception?) {}
        }
    }

    open inner class TextOnlyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val heading: TextView = view.heading
        val subHeading: TextView

        init {
            subHeading = view.subHeading
        }
    }

    inner class CardViewHolder(view: View) : TextOnlyViewHolder(view) {
        fun cancelAnimations() {
            translateTextAnim?.cancel()
            heading.translationY = 0f
            subHeading.translationY = 0f
        }

        fun setUpForTextAnim() {
            heading.translationY = 10000f
            subHeading.translationY = 10000f
        }

        val image: LoaderImageView = view.image
        var translateTextAnim: ValueAnimator? = null

    }
}