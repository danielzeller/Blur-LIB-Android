package no.danielzeller.blurbehindlib.opengl

import android.graphics.Canvas
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES
import android.opengl.GLES20.*
import android.opengl.GLUtils
import android.util.Log
import android.view.Surface

class ViewSurfaceTexture {

    private var textureWidth: Int = 0
    private var textureHeight: Int = 0

    private var surfaceTexture: SurfaceTexture? = null
    private var surface: Surface? = null

    private var surfaceTextureID = -1

    fun createSurface(width: Int, height: Int) {

        textureWidth = width
        textureHeight = height
        releaseSurface()
        surfaceTextureID = createTexture()
        if (surfaceTextureID > 0) {
            surfaceTexture = SurfaceTexture(surfaceTextureID)
            surfaceTexture!!.setDefaultBufferSize(textureWidth, textureHeight)
            surface = Surface(surfaceTexture)
        }
    }

    fun updateTexture() {
        surfaceTexture?.updateTexImage()
    }

    fun releaseSurface() {
        surface?.release()
        surfaceTexture?.release()
        surface = null
        surfaceTexture = null
    }

    fun beginDraw(): Canvas? {

        if (surface != null) {
            return surface?.lockHardwareCanvas()
        }
        return null
    }

    fun endDraw(surfaceCanvas: Canvas?) {
        if (surfaceCanvas != null) {
            surface?.unlockCanvasAndPost(surfaceCanvas)
        }
    }

    private fun createTexture(): Int {
        val textures = IntArray(1)

        // Generate the texture to where android view will be rendered
        glActiveTexture(GL_TEXTURE0)
        glGenTextures(1, textures, 0)
        checkGlError("Texture generate")

        glBindTexture(GL_TEXTURE_EXTERNAL_OES, textures[0])
        checkGlError("Texture bind")

        glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MIN_FILTER, GL_LINEAR.toFloat())
        glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MAG_FILTER, GL_LINEAR.toFloat())
        glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

        return textures[0]
    }

    private fun checkGlError(op: String) {
        val error: Int = glGetError()
        if (error != GL_NO_ERROR) {
            Log.e("GL_ERROR", op + ": glError " + GLUtils.getEGLErrorString(error))
        }
    }

    fun getTextureID(): Int {
        return surfaceTextureID
    }
}