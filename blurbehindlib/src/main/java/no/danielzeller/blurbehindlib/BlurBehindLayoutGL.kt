package no.danielzeller.blurbehindlib

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.Choreographer
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import no.opengl.danielzeller.opengltesting.opengl.util.FrameRateCounter

class BlurBehindLayoutGL : FrameLayout {

    private lateinit var glSurfaceView: GLSurfaceView
    protected lateinit var openglGLRenderer: GLSurfaceViewRenderer

    var viewBehind: View? = null
    val contentBehindRect = Rect()
    val blurViewRect = Rect()
    var fpsTextView: TextView? = null

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        initView(context)
    }

    fun initView(context: Context) {
        glSurfaceView = GLSurfaceView(context)
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.setBackgroundColor(Color.TRANSPARENT)
//        glSurfaceView.setZOrderOnTop(true)
//        glSurfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 0, 0);
        openglGLRenderer = GLSurfaceViewRenderer(context, 0.99f)
        glSurfaceView.setRenderer(openglGLRenderer)
        addView(glSurfaceView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        Choreographer.getInstance().postFrameCallback { redrawContent() }
    }

    fun redrawContent() {

        if (openglGLRenderer.isCreated) {
            val glCanvas = openglGLRenderer.surfaceTexture.beginDraw()

            glCanvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            viewBehind?.getHitRect(contentBehindRect)
            getHitRect(blurViewRect)
            glCanvas?.scale(openglGLRenderer.scale, openglGLRenderer.scale)
            glCanvas?.translate((blurViewRect.left - contentBehindRect.left).toFloat(), (contentBehindRect.top - blurViewRect.top).toFloat())
            if (glCanvas != null)
                viewBehind?.draw(glCanvas)

            openglGLRenderer.surfaceTexture.endDraw(glCanvas)
            FrameRateCounter.timeStep()

            val msec = FrameRateCounter.deltaTime2 * 1000.0f
            val fps = 1.0f / FrameRateCounter.deltaTime2
            fpsTextView?.text = " fps "+(fps+1.0f).toInt()



        }
        Choreographer.getInstance().postFrameCallback { redrawContent() }
    }

    fun setBlurRadius(value: Float) {
        openglGLRenderer.blurRadius = value
    }

    fun setBlurType(mode: BlurMode) {
        openglGLRenderer.blurMode = mode
    }


}