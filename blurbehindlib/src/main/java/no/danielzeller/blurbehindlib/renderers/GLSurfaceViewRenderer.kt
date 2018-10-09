package no.danielzeller.blurbehindlib.renderers

import android.content.Context
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView.Renderer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLSurfaceViewRenderer(private val context: Context, internal val scale: Float) : Renderer {

    lateinit var commonRenderer: CommonRenderer

    override fun onSurfaceCreated(glUnused: GL10, config: EGLConfig) {
        glClear(GL_COLOR_BUFFER_BIT)
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        commonRenderer.onSurfaceCreated()
    }

    override fun onSurfaceChanged(glUnused: GL10, width: Int, height: Int) {
        glClear(GL_COLOR_BUFFER_BIT)
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        commonRenderer.onSurfaceChanged(width, height)
    }

    override fun onDrawFrame(glUnused: GL10) {
        glClear(GL_COLOR_BUFFER_BIT)
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

        commonRenderer.onDrawFrame()
    }
}
