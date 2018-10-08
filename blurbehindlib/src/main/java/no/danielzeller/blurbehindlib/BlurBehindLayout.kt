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
import no.danielzeller.blurbehindlib.renderers.CommonRenderer
import no.danielzeller.blurbehindlib.renderers.GLSurfaceViewRenderer
import no.danielzeller.blurbehindlib.renderers.TextureViewRenderer
import no.opengl.danielzeller.opengltesting.opengl.util.FrameRateCounter

class BlurBehindLayout : FrameLayout {

    var viewBehind: View? = null
        set(value) {
            field = value
        }

    private val viewBehindRect = Rect()
    private val thisViewRect = Rect()
    private var useTextureView = true
    private lateinit var commonRenderer: CommonRenderer
    private val scale = 0.4f
    var currentFPS = 0f

    constructor(context: Context, useTextureView: Boolean) : super(context) {
        initView(context)
        this.useTextureView = useTextureView
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        initView(context)
    }

    fun initView(context: Context) {
        setWillNotDraw(false)
        commonRenderer = CommonRenderer(context, scale)
        if (useTextureView) {
            createTextureView(context)
        } else {
            createGLSurfaceView(context)
        }
        Choreographer.getInstance().postFrameCallback { redrawBlurTexture() }
    }


    private lateinit var renderView: View

    fun createGLSurfaceView(context: Context) {
        var glSurfaceView = GLSurfaceView(context)
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.setBackgroundColor(Color.TRANSPARENT)
        glSurfaceView.setZOrderOnTop(false)
        glSurfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 0, 0);
        addView(glSurfaceView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        var openglGLRenderer = GLSurfaceViewRenderer(context, 0.3f)
        glSurfaceView.setRenderer(openglGLRenderer)
        openglGLRenderer.commonRenderer = commonRenderer
        renderView = glSurfaceView
    }

    private lateinit var textureViewRenderer: TextureViewRenderer
    fun createTextureView(context: Context) {
        var textureView = TextureView(context)
        textureViewRenderer = TextureViewRenderer(context, 0.3f)
        textureView.surfaceTextureListener = textureViewRenderer
        addView(textureView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        textureViewRenderer.commonRenderer = commonRenderer
        renderView = textureView
    }


    fun redrawBlurTexture() {

        if (commonRenderer.isCreated) {
            val glCanvas = commonRenderer.surfaceTexture.beginDraw()
            glCanvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            viewBehind?.getHitRect(viewBehindRect)
            getHitRect(thisViewRect)
            glCanvas?.scale(commonRenderer.scale, commonRenderer.scale)
            glCanvas?.translate((thisViewRect.left - viewBehindRect.left).toFloat(), (viewBehindRect.top - thisViewRect.top + commonRenderer.paddingTop * 0.5f).toFloat())
            visibility = View.INVISIBLE
            viewBehind?.draw(glCanvas)
            visibility = View.VISIBLE
            commonRenderer.surfaceTexture.endDraw(glCanvas)
            if (useTextureView) {
                textureViewRenderer.update()
            }
            captureFPS()
        }
        Choreographer.getInstance().postFrameCallback { redrawBlurTexture() }
    }

    fun captureFPS() {
        FrameRateCounter.timeStep()
        currentFPS = 1.0f / FrameRateCounter.deltaTime2
        Log.i("FPS", "FPS: " + currentFPS)
    }

    fun setBlurRadius(value: Float) {
        commonRenderer.blurRadius = value
    }
}