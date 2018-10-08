package no.danielzeller.blurbehindlib

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.Rect
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.Choreographer
import android.view.TextureView
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import no.danielzeller.blurbehindlib.renderers.CommonRenderer
import no.danielzeller.blurbehindlib.renderers.GLSurfaceViewRenderer
import no.danielzeller.blurbehindlib.renderers.TextureViewRenderer
import no.opengl.danielzeller.opengltesting.opengl.util.FrameRateCounter

enum class UpdateMode {
    CONTINOUSLY, ON_SCROLL, MANUALLY
}

class BlurBehindLayout : FrameLayout {

    var viewBehind: View? = null
        set(value) {
            field = value
        }

    var updateMode = UpdateMode.CONTINOUSLY
        set(value) {
            viewTreeObserver.removeOnScrollChangedListener(onScrollChangesListener)
            if (value == UpdateMode.ON_SCROLL) {
                addOnScrollListener()
            }
            field = value
        }

    var currentFPS = 0f

    private val viewBehindRect = Rect()
    private val thisViewRect = Rect()
    private var useTextureView = false
    private lateinit var commonRenderer: CommonRenderer
    private val scale = 0.4f
    private lateinit var textureViewRenderer: TextureViewRenderer
    private lateinit var renderView: View
    private var updateViewUntil = -1L

    constructor(context: Context, useTextureView: Boolean) : super(context) {
        initView(context)
        this.useTextureView = useTextureView
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        loadAttributesFromXML(attributeSet)
        initView(context)
    }

    fun setBlurRadius(value: Float) {
        commonRenderer.blurRadius = value
    }

    fun updateForMilliSeconds(milliSeconds: Long) {
        updateViewUntil = System.currentTimeMillis() + milliSeconds
        Choreographer.getInstance().removeFrameCallback(frameCallBack)
        Choreographer.getInstance().postFrameCallback(frameCallBack)
    }

    private fun initView(context: Context) {
        setWillNotDraw(false)
        commonRenderer = CommonRenderer(context, scale)
        if (useTextureView) {
            createTextureView(context)
        } else {
            createGLSurfaceView(context)
        }
    }

    private fun loadAttributesFromXML(attrs: AttributeSet?) {

        val typedArray = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.Blur,
                0, 0)
        try {
            useTextureView = typedArray.getBoolean(R.styleable.Blur_useTextureView, true)
            updateMode = convertIntToEnum(typedArray.getInteger(R.styleable.Blur_updateMode, updateMode.ordinal))
        } finally {
            typedArray.recycle()
        }
    }

    private fun addOnScrollListener() {
        viewTreeObserver.addOnScrollChangedListener(onScrollChangesListener)
    }

    private val onScrollChangesListener = object : ViewTreeObserver.OnScrollChangedListener {
        override fun onScrollChanged() {
            updateForMilliSeconds(200)
        }
    }

    private fun createGLSurfaceView(context: Context) {
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

    private fun createTextureView(context: Context) {
        var textureView = TextureView(context)
        textureViewRenderer = TextureViewRenderer(context, 0.3f)
        textureView.surfaceTextureListener = textureViewRenderer
        addView(textureView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        textureViewRenderer.commonRenderer = commonRenderer
        renderView = textureView
    }

    private fun redrawBlurTexture() {
        if (commonRenderer.isCreated) {
            renderBehindViewToTexture()
            if (useTextureView) {
                textureViewRenderer.update()
            }
            captureFPS()
        }
        if (updateMode == UpdateMode.CONTINOUSLY || System.currentTimeMillis() < updateViewUntil) {
            Choreographer.getInstance().postFrameCallback(frameCallBack)
        }
    }

    var frameCallBack = Choreographer.FrameCallback {
        redrawBlurTexture()
    }

    private fun renderBehindViewToTexture() {
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
    }

    private fun captureFPS() {
        FrameRateCounter.timeStep()
        currentFPS = 1.0f / FrameRateCounter.deltaTime2
    }

    private fun convertIntToEnum(id: Int): UpdateMode {
        for (f in UpdateMode.values()) {
            if (f.ordinal == id) return f
        }
        return UpdateMode.CONTINOUSLY
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Choreographer.getInstance().removeFrameCallback(frameCallBack)
        viewTreeObserver.removeOnScrollChangedListener(onScrollChangesListener)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateForMilliSeconds(500)
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == View.VISIBLE) {
            updateForMilliSeconds(100)
        } else {
            Choreographer.getInstance().removeFrameCallback(frameCallBack)
        }
    }
}