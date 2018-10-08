package no.danielzeller.blurbehindlib.renderers

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.*
import android.opengl.EGL14.EGL_OPENGL_ES2_BIT
import android.opengl.GLES20.*
import android.view.TextureView
import no.danielzeller.blurbehindlib.R
import no.danielzeller.blurbehindlib.ShaderProgram
import no.danielzeller.blurbehindlib.SpriteMesh
import no.danielzeller.blurbehindlib.TextureShaderProgram
import no.opengl.danielzeller.opengltesting.opengl.gameobject.RenderTexture
import java.nio.IntBuffer
import javax.microedition.khronos.egl.*
import javax.microedition.khronos.egl.EGL10.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay
import javax.microedition.khronos.egl.EGLSurface


class TextureViewRenderer(val context: Context, internal val scale: Float) : TextureView.SurfaceTextureListener {

    private lateinit var renderer: RendererThread
    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {}
    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {}

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        renderer.destroy()
        return false
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        renderer = RendererThread(surface, width, height)
        renderer.initGL()
    }

    fun update() {
        renderer.update()
    }

    lateinit var commonRenderer: CommonRenderer

    inner class RendererThread(private val surface: SurfaceTexture, private val width: Int, private val height: Int) {

        var egl: EGL10? = null
        var eglDisplay: EGLDisplay? = null
        var eglContext: EGLContext? = null
        var eglSurface: EGLSurface? = null

        fun initGL() {
            egl = EGLContext.getEGL() as EGL10
            eglDisplay = egl?.eglGetDisplay(EGL_DEFAULT_DISPLAY)
            egl?.eglInitialize(eglDisplay, intArrayOf(0, 0))
            val eglConfig = chooseEglConfig(egl!!, eglDisplay!!)
            eglContext = egl!!.eglCreateContext(eglDisplay, eglConfig, EGL_NO_CONTEXT, intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL_NONE))
            eglSurface = egl!!.eglCreateWindowSurface(eglDisplay, eglConfig, surface, null)

            update()
        }



        fun update() {

            if (eglDisplay != null) {
                val brah = (EGLContext.getEGL() as EGL10).eglGetCurrentContext()

                egl!!.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)

                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
                GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
                if (!commonRenderer.isCreated) {
                    commonRenderer.onSurfaceCreated()
                    commonRenderer.onSurfaceChanged(width, height)
                }

                commonRenderer.onDrawFrame()

                egl!!.eglSwapBuffers(eglDisplay, eglSurface)
            }
        }

        fun destroy() {
            surface.release()
            commonRenderer.surfaceTexture.releaseSurface()
            egl?.eglDestroyContext(eglDisplay, eglContext)
            egl?.eglDestroySurface(eglDisplay, eglSurface)
        }

        private val config = intArrayOf(
                EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                EGL_RED_SIZE, 8,
                EGL_GREEN_SIZE, 8,
                EGL_BLUE_SIZE, 8,
                EGL_ALPHA_SIZE, 8,
                EGL_DEPTH_SIZE, 0,
                EGL_STENCIL_SIZE, 0,
                EGL_NONE
        )

        private fun chooseEglConfig(egl: EGL10, eglDisplay: EGLDisplay): EGLConfig {
            val configsCount = intArrayOf(0)
            val configs = arrayOfNulls<EGLConfig>(1)
            egl.eglChooseConfig(eglDisplay, config, configs, 1, configsCount)
            return configs[0]!!
        }
    }
}