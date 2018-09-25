package no.danielzeller.blurbehindlib

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.EGL14.EGL_OPENGL_ES2_BIT
import android.opengl.GLES20
import android.opengl.Matrix
import android.os.Handler
import android.view.TextureView
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGL10.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay

class TextureViewRenderer(val context: Context) : TextureView.SurfaceTextureListener {

    var isCreated = false
    var cutoffFactor = 0.65f
    var onSurfaceTextureCreated: (() -> Unit)? = null
    val surfaceTexture = ViewSurfaceTexture()

    private lateinit var renderer: RendererThread

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {}

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {}

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        renderer.isStopped = true
        return false
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        renderer = RendererThread(surface, width, height)
        renderer.start()
    }

    inner class RendererThread(private val surface: SurfaceTexture, private val width: Int, private val height: Int) : Thread() {

        var isStopped = false
        private val projectionMatrixOrtho = FloatArray(16)
        private lateinit var spriteMesh: SpriteMesh
        private val fullscreenTextureShader = TextureShaderProgram(R.raw.vertex_shader, R.raw.texture_fragment_shader)
        private val handler = Handler()

        override fun run() {
            super.run()

            val egl = EGLContext.getEGL() as EGL10
            val eglDisplay = egl.eglGetDisplay(EGL_DEFAULT_DISPLAY)
            egl.eglInitialize(eglDisplay, intArrayOf(0, 0))
            val eglConfig = chooseEglConfig(egl, eglDisplay)
            val eglContext = egl.eglCreateContext(eglDisplay, eglConfig, EGL_NO_CONTEXT, intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL_NONE))
            val eglSurface = egl.eglCreateWindowSurface(eglDisplay, eglConfig, surface, null)


            while (!isStopped && egl.eglGetError() == EGL_SUCCESS) {
                egl.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)

                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
                GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
                if (!isCreated) {
                    onSurfaceCreated()
                    isCreated = true
                }

                renderFullscreenRenderTexture()
                egl.eglSwapBuffers(eglDisplay, eglSurface)

                Thread.sleep((1f / 60f * 1000f).toLong())
            }

            surface.release()
            surfaceTexture.releaseSurface()
            egl.eglDestroyContext(eglDisplay, eglContext)
            egl.eglDestroySurface(eglDisplay, eglSurface)
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

        private fun onSurfaceCreated() {
            setupViewPort()
            createRenderingObjects()
            clearViewSurfaceTexture()
            handler.post{ onSurfaceTextureCreated?.invoke() }
        }

        private fun clearViewSurfaceTexture() {
            val canvas = surfaceTexture.beginDraw()
            canvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            surfaceTexture.endDraw(canvas)
        }

        private fun createRenderingObjects() {
            spriteMesh = SpriteMesh()
            fullscreenTextureShader.load(context)
            surfaceTexture.createSurface(width, height)
        }

        private fun setupViewPort() {
            GLES20.glViewport(0, 0, width, height)
            val left = -1.0f
            val right = 1.0f
            val bottom = 1.0f
            val top = -1.0f
            val near = -1.0f
            val far = 1.0f

            Matrix.setIdentityM(projectionMatrixOrtho, 0)
            Matrix.orthoM(projectionMatrixOrtho, 0, left, right, bottom, top, near, far)
        }

        private fun renderFullscreenRenderTexture() {
            surfaceTexture.updateTexture()
            fullscreenTextureShader.useProgram()
            fullscreenTextureShader.setUniforms(projectionMatrixOrtho, surfaceTexture.getTextureID(), cutoffFactor)
            spriteMesh.bindData(fullscreenTextureShader)
            spriteMesh.draw()
        }
    }
}