package no.danielzeller.blurbehindlib.renderers

import android.content.Context
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView.Renderer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

enum class BlurMode(value: Int) {
    BOX(1),
    STACK(2),
    GAUSS_2_PASS(3),
    GAUSS_1_PASS(4)
}

class GLSurfaceViewRenderer(private val context: Context, internal val scale: Float) : Renderer {

    lateinit var commonRenderer: CommonRenderer

    override fun onSurfaceCreated(glUnused: GL10, config: EGLConfig) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        commonRenderer.onSurfaceCreated()
    }

    override fun onSurfaceChanged(glUnused: GL10, width: Int, height: Int) {
        commonRenderer.onSurfaceChanged(width, height)
    }

    override fun onDrawFrame(glUnused: GL10) {
        glClear(GL_COLOR_BUFFER_BIT)
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

        commonRenderer.onDrawFrame()
    }
}
