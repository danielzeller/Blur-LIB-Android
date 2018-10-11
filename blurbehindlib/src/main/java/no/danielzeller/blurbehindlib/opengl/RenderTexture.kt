package no.danielzeller.blurbehindlib.opengl

import android.opengl.GLES20
import android.opengl.GLES20.*

class RenderTexture {

    private var fboWidth = 512
    private var fboHeight = 512

    private var renderTextureID: Int = 0

    internal var fboTex: Int = 0
    private var renderBufferId: Int = 0
    private var internalFormat = GL_RGBA
    private var format = GL_RGBA

    fun initiateFrameBuffer(width: Int, height: Int): Int {

        fboWidth = width
        fboHeight = height
        val temp = IntArray(1)
        //generate fbo id
        GLES20.glGenFramebuffers(1, temp, 0)
        renderTextureID = temp[0]
        //generate texture
        GLES20.glGenTextures(1, temp, 0)
        fboTex = temp[0]
        //generate render buffer
        GLES20.glGenRenderbuffers(1, temp, 0)
        renderBufferId = temp[0]

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, renderTextureID)
        //
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fboTex)

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, GLES20.GL_UNSIGNED_BYTE, null)

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)

        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, renderBufferId)
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, width, height)

        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, fboTex, 0)
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, renderBufferId)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        return renderTextureID
    }

    fun bindRenderTexture() {

        GLES20.glEnable(GLES20.GL_TEXTURE_2D)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, renderTextureID)
    }

    fun unbindRenderTexture() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
    }

    fun deleteAllTextures() {
        GLES20.glDeleteBuffers(1, intArrayOf(renderTextureID), 0)
        GLES20.glDeleteRenderbuffers(1, intArrayOf(renderBufferId), 0)
        GLES20.glDeleteTextures(1, intArrayOf(fboTex), 0)
    }

}
