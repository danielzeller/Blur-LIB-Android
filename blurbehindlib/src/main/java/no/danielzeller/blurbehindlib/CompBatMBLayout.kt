package no.danielzeller.blurbehindlib

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.TextureView
import android.widget.FrameLayout

open class CompBatMBLayout : FrameLayout {

    protected var isPreAndroidPie = false
    protected lateinit var textureView: TextureView
    protected lateinit var textureViewRenderer: TextureViewRenderer

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        textureViewRenderer = TextureViewRenderer(context)
    }

    fun compBatAddTextureView(frameLayout: FrameLayout) {
        if (isPreAndroidPie) {
            textureView = TextureView(context)
            textureView.surfaceTextureListener = textureViewRenderer
            textureViewRenderer.onSurfaceTextureCreated = { drawTextureView() }
            frameLayout.addView(textureView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        }
    }

    protected fun drawTextureView() {
        if (isPreAndroidPie && textureViewRenderer.isCreated) {
            textureViewRenderer.cutoffFactor = getCutoffFactor()
            val glCanvas = textureViewRenderer.surfaceTexture.beginDraw()
            glCanvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

            if (glCanvas != null){
                val metaBallContainer = getChildAt(0)
                drawChild(glCanvas, metaBallContainer, drawingTime)
            }

            textureViewRenderer.surfaceTexture.endDraw(glCanvas)
        }
    }

    open fun getCutoffFactor(): Float {
        return 0.65f
    }

    override fun dispatchDraw(canvas: Canvas) {
        if (isPreAndroidPie) {
            drawChild(canvas, textureView, drawingTime)
        } else {
            super.dispatchDraw(canvas)
        }
    }

    open fun setupBaseViews(context: Context) {
        isPreAndroidPie = android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.P
    }
}