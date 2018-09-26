package no.danielzeller.blurbehindlib

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.Rect
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.view.Choreographer
import android.view.TextureView
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import no.danielzeller.blurbehindlib.renderers.BlurMode
import no.danielzeller.blurbehindlib.renderers.CommonRenderer
import no.danielzeller.blurbehindlib.renderers.GLSurfaceViewRenderer
import no.danielzeller.blurbehindlib.renderers.TextureViewRenderer
import no.opengl.danielzeller.opengltesting.opengl.util.FrameRateCounter

class BlurBehindLayout : FrameLayout {

    var viewBehind: View? = null

    private val viewBehindRect = Rect()
    private val thisViewRect = Rect()
    private var useTextureView = false
    private lateinit var commonRenderer: CommonRenderer
    private val scale = 0.4f
    constructor(context: Context, useTextureView: Boolean) : super(context) {
        initView(context)
        this.useTextureView = useTextureView
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        initView(context)
    }

    fun initView(context: Context) {

        commonRenderer = CommonRenderer(context, scale)
        if (useTextureView) {
            createTextureView(context)
        } else {
            createGLSurfaceView(context)
        }
        Choreographer.getInstance().postFrameCallback { redrawContent() }
    }

    fun createGLSurfaceView(context: Context) {
        var glSurfaceView = GLSurfaceView(context)
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.setZOrderOnTop(false)
        glSurfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 0, 0);
        addView(glSurfaceView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        var openglGLRenderer = GLSurfaceViewRenderer(context, 0.3f)
        glSurfaceView.setRenderer(openglGLRenderer)
        openglGLRenderer.commonRenderer = commonRenderer
    }

    fun createTextureView(context: Context) {
        var textureView = TextureView(context)
        val textureViewRenderer = TextureViewRenderer(context, 0.3f)
        textureView.surfaceTextureListener = textureViewRenderer
        addView(textureView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        textureViewRenderer.commonRenderer = commonRenderer
    }

    fun redrawContent() {

        if (commonRenderer.isCreated) {
            val glCanvas = commonRenderer.surfaceTexture.beginDraw()

            glCanvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            viewBehind?.getHitRect(viewBehindRect)
            getHitRect(thisViewRect)
            glCanvas?.scale(commonRenderer.scale, commonRenderer.scale)
            glCanvas?.translate((thisViewRect.left - viewBehindRect.left).toFloat(), (viewBehindRect.top - thisViewRect.top).toFloat())

            if (glCanvas != null)
                viewBehind?.draw(glCanvas)
            visibility = View.INVISIBLE
            commonRenderer.surfaceTexture.endDraw(glCanvas)
            visibility = View.VISIBLE
            FrameRateCounter.timeStep()

//            val fps = 1.0f / FrameRateCounter.deltaTime2
//            Log.i("BlurView","FPS: "+ + (fps + 1.0f).toInt())
        }

        Choreographer.getInstance().postFrameCallback { redrawContent() }
    }

    fun setBlurRadius(value: Float) {
        commonRenderer.blurRadius = value
    }

    fun setBlurType(mode: BlurMode) {
        commonRenderer.blurMode = mode
    }
}