package no.danielzeller.blurbehindlib

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.opengl.GLSurfaceView
import android.opengl.GLSurfaceView.RENDERMODE_WHEN_DIRTY
import android.util.AttributeSet
import android.view.Choreographer
import android.view.TextureView
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import no.danielzeller.blurbehindlib.renderers.CommonRenderer
import no.danielzeller.blurbehindlib.renderers.GLSurfaceViewRenderer
import no.danielzeller.blurbehindlib.renderers.TextureViewRenderer
import android.view.ViewGroup
import kotlin.IllegalStateException


enum class UpdateMode {
    CONTINUOUSLY, ON_SCROLL, MANUALLY
}

class BlurBehindLayout : FrameLayout {


    /**
     *  The View behind the BlurBehindLayout, that is to be Blurred.
     */
    var viewBehind: View? = null
        set(value) {
            checkParent(value)
            field = value
        }


    /**
     * Set the update mode. When updateMode is
     * UpdateMode.CONTINUOUSLY, the renderer is called
     * repeatedly to re-render the scene. When updateMode
     * is UpdateMode.ON_SCROLL, the renderer only renders
     * when a View is Scrolled.
     * When updateMode is UpdateMode.MANUALLY, the renderer only renders when the surface is created
     * and when updateForMilliSeconds(..) is called manually.
     *
     * @param updateMode one of the UpdateMode enums
     * @see #UpdateMode
     */
    var updateMode = UpdateMode.CONTINUOUSLY
        set(value) {
            viewTreeObserver.removeOnScrollChangedListener(onScrollChangesListener)
            if (value == UpdateMode.ON_SCROLL) {
                addOnScrollListener()
            }
            field = value
        }


    /**
     *  The blur radius. Higher value will give a stronger blur. 0f = no blur.
     */
    var blurRadius = 40f
        set(value) {

            commonRenderer?.blurRadius = value
            field = value
        }

    /**
     * If true the BlurBehindLayout will use the alpha value of the first child as clipping mask.
     * This can for instance be used to create blur behind TexViews or have rounded edges etc.
     * NOTE This will force useTextureView to be true in order to support Transparent rendering.
     */
    var useChildAlphaAsMask = false
        set(value) {
            commonRenderer?.useChildAlphaAsMask = value
            field = value
            checkTextureView()
        }

    private val thisViewPosition = intArrayOf(0, 0)
    private val behindViewPosition = intArrayOf(0, 0)
    private var useTextureView = false
    private var commonRenderer: CommonRenderer? = null
    private var blurTextureScale = 0.4f
    private lateinit var textureViewRenderer: TextureViewRenderer
    private lateinit var renderView: View
    private var updateViewUntil = -1L
    private var isBlurDisabled = false
    private var paddingVertical = 0f
    private val onScrollChangesListener = ViewTreeObserver.OnScrollChangedListener { updateForMilliSeconds(200) }

    constructor(context: Context, useTextureView: Boolean, blurTextureScale: Float, paddingVertical: Float) : super(context) {
        this.blurTextureScale = blurTextureScale
        this.useTextureView = useTextureView
        this.paddingVertical = paddingVertical
        initView(context)
    }

    constructor(context: Context, useChildAlphaAsMask: Boolean) : super(context) {
        this.useChildAlphaAsMask = useChildAlphaAsMask
        initView(context)
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        loadAttributesFromXML(attributeSet)
        initView(context)
    }


    /**
     *  This will udate the View for the given time in milliseconds. Useful for when updateMode
     *  is UpdateMode.ON_SCOLL or UpdateMode.MANUALLY. This can be can be used if there is some
     *  animation or update running in the background.
     *  @param milliSeconds How long should the View update for.
     */
    fun updateForMilliSeconds(milliSeconds: Long) {
        updateViewUntil = System.currentTimeMillis() + milliSeconds
        Choreographer.getInstance().removeFrameCallback(frameCallBack)
        Choreographer.getInstance().postFrameCallback(frameCallBack)
    }

    /**
     * Disables the blur View. Useful for when the BlurView is used in animations.
     * Setting Visibility = View.GONE on a SurfaceView will cause a black flicker in
     * some occasions. Using this method disables the blurView updates and hodes it from the
     * screen without any flicker issues.
     */
    fun disable() {
        Choreographer.getInstance().removeFrameCallback(frameCallBack)
        viewTreeObserver.removeOnScrollChangedListener(onScrollChangesListener)
        //Setting visibility=GONE causes a black flicker since the SurfaceView rendering
        //and View rendering is'nt 1-1 synced. Setting translation off the screen removes the flicker.
        //The View is'nt updated anyways since we only do a render in the frameCallBack
        renderView.translationX = 100000f
        isBlurDisabled = true
    }

    /**
     * Enables the blur View again. Should only be called if  disable() has been called first.
     */
    fun enable() {
        if (isBlurDisabled) {
            if (updateMode == UpdateMode.ON_SCROLL) {
                addOnScrollListener()
            }
            renderView.translationX = 0f
            isBlurDisabled = false
            updateForMilliSeconds(10)
        }
    }

    private fun initView(context: Context) {
        commonRenderer = CommonRenderer(context, blurTextureScale, useChildAlphaAsMask, paddingVertical)
        commonRenderer!!.blurRadius = blurRadius
        if (useTextureView) {
            createTextureView(context)
        } else {
            createGLSurfaceView(context)
        }
    }

    override fun setAlpha(alpha: Float) {
        if (useChildAlphaAsMask) {
            /**
             * There is a bug in Android, that renders black background when alpha i 1f,
             * this horrible hack fixes that.
             */
            super.setAlpha(Math.min(alpha, 0.99f))
        } else {
            super.setAlpha(alpha)
        }
    }

    private fun loadAttributesFromXML(attrs: AttributeSet?) {

        val typedArray = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.Blur,
                0, 0)
        try {
            useTextureView = typedArray.getBoolean(R.styleable.Blur_useTextureView, false)
            useChildAlphaAsMask = typedArray.getBoolean(R.styleable.Blur_useChildAlphaAsMask, false)
            updateMode = convertIntToEnum(typedArray.getInteger(R.styleable.Blur_updateMode, updateMode.ordinal))
            blurRadius = typedArray.getFloat(R.styleable.Blur_blurRadius, blurRadius)
            blurTextureScale = typedArray.getFloat(R.styleable.Blur_blurTextureScale, blurTextureScale)
            paddingVertical = typedArray.getFloat(R.styleable.Blur_blurPaddingVertical, resources.getDimension(R.dimen.defaultVerticalPadding))

        } finally {
            typedArray.recycle()
        }
    }


    private fun addOnScrollListener() {
        viewTreeObserver.addOnScrollChangedListener(onScrollChangesListener)
    }

    private fun createGLSurfaceView(context: Context) {
        val openGLRenderer = GLSurfaceViewRenderer()

        val glSurfaceView = GLSurfaceView(context)
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.setZOrderMediaOverlay(true)
        glSurfaceView.setRenderer(openGLRenderer)
        glSurfaceView.renderMode = RENDERMODE_WHEN_DIRTY

        addView(glSurfaceView)
        openGLRenderer.commonRenderer = commonRenderer!!

        renderView = glSurfaceView
    }

    private fun createTextureView(context: Context) {
        textureViewRenderer = TextureViewRenderer(context)

        val textureView = TextureView(context)
        textureView.surfaceTextureListener = textureViewRenderer

        addView(textureView)
        textureViewRenderer.commonRenderer = commonRenderer!!
        renderView = textureView
    }

    private fun redrawBlurTexture() {
        if (commonRenderer!!.isCreated && renderView.visibility == View.VISIBLE) {
            renderBehindViewToTexture()
            renderChildViewToTexture()
            updateRenderView()
        }
        if (updateMode == UpdateMode.CONTINUOUSLY || System.currentTimeMillis() < updateViewUntil) {
            Choreographer.getInstance().postFrameCallback(frameCallBack)
        }
    }

    private fun updateRenderView() {
        if (useTextureView) {
            textureViewRenderer.update()
        } else {
            (renderView as GLSurfaceView).requestRender()
        }
    }

    private var frameCallBack = Choreographer.FrameCallback {
        redrawBlurTexture()
    }

    private fun renderBehindViewToTexture() {
        val commonRenderer = commonRenderer!!

        val glCanvas = commonRenderer.behindViewSurfaceTexture.beginDraw()

        glCanvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        viewBehind?.getLocationInWindow(behindViewPosition)
        getLocationInWindow(thisViewPosition)

        glCanvas?.scale(commonRenderer.scale, commonRenderer.scale)
        glCanvas?.translate(0f, paddingVertical * 0.5f)
        val behindMatrix = viewBehind?.matrix
        behindMatrix?.postTranslate(behindViewPosition[0] - thisViewPosition[0].toFloat() - paddingLeft, behindViewPosition[1] - thisViewPosition[1].toFloat() - paddingTop)
        glCanvas?.concat(behindMatrix)

        viewBehind?.draw(glCanvas)

        commonRenderer.behindViewSurfaceTexture.endDraw(glCanvas)
    }

    private fun renderChildViewToTexture() {
        if (useChildAlphaAsMask) {
            if (childCount > 1) {
                val childView = getChildAt(1)
                /**
                 * Since the openGL drawing is falling a couple of frames behind the regular UI drawing
                 * we render the child in openGL instead of the regular UI drawcall, so that it is
                 * synced with the openGL drawCommands.
                 */
                if (childView.visibility != INVISIBLE) {
                    childView.visibility = INVISIBLE
                }
                val commonRenderer = commonRenderer!!
                val glCanvas = commonRenderer.childViewSurfaceTexture.beginDraw()

                glCanvas?.scale(1f, height.toFloat() / (height.toFloat() + paddingVertical))
                glCanvas?.translate(childView.left.toFloat(), childView.top + paddingVertical * 0.5f)
                glCanvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                glCanvas?.concat(childView.matrix)
                childView.draw(glCanvas)

                commonRenderer.childViewSurfaceTexture.endDraw(glCanvas)
            }
        }
    }

    private fun convertIntToEnum(id: Int): UpdateMode {
        for (f in UpdateMode.values()) {
            if (f.ordinal == id) return f
        }
        return UpdateMode.CONTINUOUSLY
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Choreographer.getInstance().removeFrameCallback(frameCallBack)
        viewTreeObserver.removeOnScrollChangedListener(onScrollChangesListener)
        commonRenderer?.destroyResources()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateForMilliSeconds(500)
    }


    private fun checkTextureView() {
        if (!useTextureView && useChildAlphaAsMask)
            throw IllegalStateException("useChildAlphaAsMask=true requires useTextureView=true")
    }

    private fun checkParent(value: View?) {
        if (value != null && value is ViewGroup) {
            recursiveLoopChildren(value)
        }
    }

    private fun recursiveLoopChildren(parent: ViewGroup) {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            if (child == this)
                throw IllegalStateException("Blur Lib: The blurbehind view cannot be a parent of the BlurBehindLayout")
            if (child is ViewGroup) {
                recursiveLoopChildren(child)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        renderView.layoutParams = LayoutParams(0, 0);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        renderView.measure(widthMeasureSpec, heightMeasureSpec);
    }
}