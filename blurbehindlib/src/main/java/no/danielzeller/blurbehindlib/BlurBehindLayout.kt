package no.danielzeller.blurbehindlib

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.util.AttributeSet
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout

class BlurBehindLayout : FrameLayout {

    private lateinit var textureView: TextureView
    protected lateinit var textureViewRenderer: TextureViewRenderer

    var viewBehind: View? = null
    val contentBehindRect = Rect()
    val blurViewRect = Rect()

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        initView(context)
    }

    fun initView(context: Context) {
        textureView = TextureView(context)
        addView(textureView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        textureViewRenderer = TextureViewRenderer(context)
        textureView.surfaceTextureListener = textureViewRenderer
        textureViewRenderer.onSurfaceTextureCreated = { }
        viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                redrawContent()
                return true
            }
        })
    }

    fun redrawContent() {

        if (textureViewRenderer.isCreated) {
            val glCanvas = textureViewRenderer.surfaceTexture.beginDraw()
            glCanvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            glCanvas?.drawColor(Color.RED)
            viewBehind?.getHitRect(contentBehindRect)
            getHitRect(blurViewRect)
            glCanvas?.translate((blurViewRect.left - contentBehindRect.left).toFloat(), (contentBehindRect.top - blurViewRect.top).toFloat())
            (viewBehind!! as ViewGroup).draw(glCanvas)

            textureViewRenderer.surfaceTexture.endDraw(glCanvas)
        }
    }

}