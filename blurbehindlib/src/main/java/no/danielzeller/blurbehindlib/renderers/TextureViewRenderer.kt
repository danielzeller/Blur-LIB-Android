package no.danielzeller.blurbehindlib.renderers

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.*
import android.opengl.EGL14.EGL_OPENGL_ES2_BIT
import android.opengl.GLES20.glBlendFunc
import android.opengl.GLES20.glEnable
import android.view.TextureView
import javax.microedition.khronos.egl.*
import javax.microedition.khronos.egl.EGL10.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay
import javax.microedition.khronos.egl.EGLSurface

class TextureViewRenderer(val context: Context) : TextureView.SurfaceTextureListener {

    private lateinit var textureViewRenderThread: TextureViewRenderThread
    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {}
    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {}

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        textureViewRenderThread.isStopped = true
        return false
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        textureViewRenderThread = TextureViewRenderThread(surface, width, height)
        textureViewRenderThread.start()
    }

    fun update() {
        textureViewRenderThread.update = true
    }

    lateinit var commonRenderer: CommonRenderer

    inner class TextureViewRenderThread(private val surface: SurfaceTexture, private val width: Int, private val height: Int) : Thread() {

        private var egl: EGL10? = null
        private var eglDisplay: EGLDisplay? = null
        private var eglContext: EGLContext? = null
        private var eglSurface: EGLSurface? = null
        var isStopped = false

        var update = true

        private fun initGL() {
            egl = EGLContext.getEGL() as EGL10
            eglDisplay = egl?.eglGetDisplay(EGL_DEFAULT_DISPLAY)
            egl?.eglInitialize(eglDisplay, intArrayOf(0, 0))
            val eglConfig = chooseEglConfig(egl!!, eglDisplay!!)
            eglContext = egl!!.eglCreateContext(eglDisplay, eglConfig, EGL_NO_CONTEXT, intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL_NONE))
            eglSurface = egl!!.eglCreateWindowSurface(eglDisplay, eglConfig, surface, null)

        }

        override fun run() {
            super.run()
            initGL()
            while (!isStopped && egl?.eglGetError() == EGL_SUCCESS) {
                while (!update) {
                    Thread.sleep((1f / 90f * 1000f).toLong())
                }
                egl!!.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)

                GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

                if (!commonRenderer.isCreated) {
                    commonRenderer.onSurfaceCreated()
                    commonRenderer.onSurfaceChanged(width, height)
                    glEnable(GLES20.GL_BLEND)
                    glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
                }

                commonRenderer.onDrawFrame()

                egl!!.eglSwapBuffers(eglDisplay, eglSurface)
                update = false
                Thread.sleep((1f / 60f * 1000f).toLong())
            }
            destroyResources()
        }

        private fun destroyResources() {
            surface.release()
            commonRenderer.behindViewSurfaceTexture.releaseSurface()
            egl?.eglDestroyContext(eglDisplay, eglContext)
            egl?.eglDestroySurface(eglDisplay, eglSurface)
        }

        private val config = intArrayOf(
                EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                EGL_RED_SIZE, 8,
                EGL_GREEN_SIZE, 8,
                EGL_BLUE_SIZE, 8,
                EGL_ALPHA_SIZE, 8,
                EGL_DEPTH_SIZE, 16,
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