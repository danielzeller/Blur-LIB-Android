package no.danielzeller.blurbehindlib.renderers

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLES20.*
import android.opengl.GLES30
import android.opengl.Matrix
import no.danielzeller.blurbehindlib.*
import no.danielzeller.blurbehindlib.opengl.*


class CommonRenderer(private val context: Context, internal val scale: Float, var useChildAlphaAsMask: Boolean, private val paddingVertical: Float) {

    var behindViewSurfaceTexture = ViewSurfaceTexture()
    var childViewSurfaceTexture = ViewSurfaceTexture()
    var isCreated = false
    var blurRadius = 40f

    private val projectionMatrixOrtho = FloatArray(16)
    private lateinit var spriteMesh: SpriteMesh

    private val fullscreenTextureShader = TextureShaderProgram(R.raw.vertex_shader, R.raw.texture_frag)
    private val fullscreenMaskTextureShader = TextureShaderProgram(R.raw.vertex_shader, R.raw.texture_and_mask_frag)

    private val gauss2PassHorizontal = TextureShaderProgram(R.raw.vertex_shader, R.raw.gauss_2_pass_horizontal)
    private val gauss2PassVertical = TextureShaderProgram(R.raw.vertex_shader, R.raw.gauss_2_pass_vertical)
    private var renderTextureHorizontal = RenderTexture()
    private var renderTextureVertical = RenderTexture()
    private var width = 0
    private var height = 0

    fun onSurfaceCreated() {
        glDisable(GL_DEPTH_TEST)
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        spriteMesh = SpriteMesh()
        fullscreenTextureShader.load(context)
        fullscreenMaskTextureShader.load(context)
        gauss2PassHorizontal.load(context)
        gauss2PassVertical.load(context)
    }

    fun onSurfaceChanged(width: Int, height: Int) {
        this.width = width
        this.height = height
        renderTextureHorizontal.initiateFrameBuffer((width * scale).toInt(), ((height + paddingVertical) * scale).toInt())
        renderTextureVertical.initiateFrameBuffer((width * scale).toInt(), ((height + paddingVertical) * scale).toInt())
        behindViewSurfaceTexture.createSurface((width * scale).toInt(), ((height + paddingVertical) * scale).toInt())
        childViewSurfaceTexture.createSurface(width, height)
        clearViewSurfaceTexture()
        isCreated = true
    }


    private fun clearViewSurfaceTexture() {
        val canvas = behindViewSurfaceTexture.beginDraw()
        canvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        behindViewSurfaceTexture.endDraw(canvas)
    }

    private fun setupViewPort(width: Int, height: Int, offset: Float) {
        GLES20.glViewport(0, -(offset * 0.5f).toInt(), width, (height + offset).toInt())
        val left = -1.0f
        val right = 1.0f
        val bottom = 1f
        val top = -1f
        val near = -1.0f
        val far = 1.0f
        Matrix.setIdentityM(projectionMatrixOrtho, 0)
        Matrix.orthoM(projectionMatrixOrtho, 0, left, right, bottom, top, near, far)
    }

    internal fun onDrawFrame() {

        behindViewSurfaceTexture.updateTexture()
        blurPass(renderTextureHorizontal, gauss2PassHorizontal, false, behindViewSurfaceTexture.getTextureID())
        blurPass(renderTextureVertical, gauss2PassVertical, true, renderTextureHorizontal.fboTex)
        if (useChildAlphaAsMask) {
            renderFullscreenTexture(fullscreenMaskTextureShader)
        } else {
            renderFullscreenTexture(fullscreenTextureShader)
        }
    }


    private fun renderFullscreenTexture(shader: TextureShaderProgram) {
        setupViewPort(width, height, paddingVertical)
        shader.useProgram()
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(shader.program, ShaderProgram.U_MATRIX), 1, false, projectionMatrixOrtho, 0)

        GLES20.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, renderTextureVertical.fboTex)
        GLES20.glUniform1i(GLES20.glGetUniformLocation(shader.program, "mainTexture"), 0)

        if (useChildAlphaAsMask) {
            childViewSurfaceTexture.updateTexture()
            GLES20.glActiveTexture(GLES30.GL_TEXTURE1)
            GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, childViewSurfaceTexture.getTextureID())
            GLES20.glUniform1i(GLES20.glGetUniformLocation(shader.program, "maskTexture"), 1)
        }
        spriteMesh.bindData(shader)
        spriteMesh.draw()
    }


    private fun blurPass(renderTexture: RenderTexture, blurShader: TextureShaderProgram, isVerticalPass: Boolean, bindTextureID: Int) {
        setupViewPort((width * scale).toInt(), ((height + paddingVertical) * scale).toInt(), 0f)
        renderTexture.bindRenderTexture()
        blurShader.useProgram()

        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(blurShader.program, ShaderProgram.U_MATRIX), 1, false, projectionMatrixOrtho, 0)

        GLES20.glActiveTexture(GLES30.GL_TEXTURE0)
        if (bindTextureID != behindViewSurfaceTexture.getTextureID()) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, bindTextureID)
        } else {
            GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, bindTextureID)
        }
        GLES20.glUniform1i(GLES20.glGetUniformLocation(blurShader.program, "mainTexture"), 0)

        if (isVerticalPass) {
            GLES20.glUniform1f(GLES20.glGetUniformLocation(blurShader.program, "textureWidth"), 1f / width.toFloat() / scale)
            GLES20.glUniform1f(GLES20.glGetUniformLocation(blurShader.program, "textureHeight"), 0f)
        } else {
            GLES20.glUniform1f(GLES20.glGetUniformLocation(blurShader.program, "textureWidth"), 0f)
            GLES20.glUniform1f(GLES20.glGetUniformLocation(blurShader.program, "textureHeight"), 1f / height.toFloat() / scale)
        }

        GLES20.glUniform1f(GLES20.glGetUniformLocation(blurShader.program, "scale"), scale)
        GLES20.glUniform1i(GLES20.glGetUniformLocation(blurShader.program, "blurRadius"), (blurRadius * scale).toInt())

        spriteMesh.bindData(blurShader)
        spriteMesh.draw()
        renderTexture.unbindRenderTexture()
    }

    fun destroyResources() {
        GLES20.glDeleteProgram(fullscreenTextureShader.program)
        GLES20.glDeleteProgram(fullscreenMaskTextureShader.program)
        GLES20.glDeleteProgram(gauss2PassHorizontal.program)
        GLES20.glDeleteProgram(gauss2PassVertical.program)

        behindViewSurfaceTexture.releaseSurface()
        renderTextureHorizontal.deleteAllTextures()
        renderTextureVertical.deleteAllTextures()
    }
}