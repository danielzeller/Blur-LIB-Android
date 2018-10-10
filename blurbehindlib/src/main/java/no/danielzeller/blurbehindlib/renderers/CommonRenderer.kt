package no.danielzeller.blurbehindlib.renderers

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.Matrix
import android.util.Log
import no.danielzeller.blurbehindlib.*
import no.opengl.danielzeller.opengltesting.opengl.gameobject.RenderTexture

class CommonRenderer(private val context: Context, internal val scale: Float) {

    private val projectionMatrixOrtho = FloatArray(16)
    private lateinit var spriteMesh: SpriteMesh
    private val fullscreenTextureShader = TextureShaderProgram(R.raw.vertex_shader, R.raw.texture_fragment_shader)

    private val gauss2PassHorizontal = TextureShaderProgram(R.raw.vertex_shader, R.raw.gauss_2_pass_horizontal)
    private val gauss2PassVertical = TextureShaderProgram(R.raw.vertex_shader, R.raw.gauss_2_pass_vertical)

    var surfaceTexture = ViewSurfaceTexture()
    var isCreated = false

    private var width = 0
    private var height = 0
    private var renderTextureHorizontal = RenderTexture()
    private var renderTextureVertical = RenderTexture()

    var blurRadius = 40f
    var paddingTop = 0f

    fun onSurfaceCreated() {

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

        spriteMesh = SpriteMesh()
        fullscreenTextureShader.load(context)

        gauss2PassHorizontal.load(context)
        gauss2PassVertical.load(context)
    }

    fun onSurfaceChanged(width: Int, height: Int) {
        this.width = width
        this.height = height
        paddingTop = 50f * context.resources.displayMetrics.density
        renderTextureHorizontal.InitiateFrameBuffer((width * scale).toInt(), ((height + paddingTop) * scale).toInt())
        renderTextureVertical.InitiateFrameBuffer((width * scale).toInt(), ((height + paddingTop) * scale).toInt())
        surfaceTexture.createSurface((width * scale).toInt(), ((height + paddingTop) * scale).toInt())
        clearViewSurfaceTexture()
        isCreated = true
    }


    private fun clearViewSurfaceTexture() {
        val canvas = surfaceTexture.beginDraw()
        canvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        surfaceTexture.endDraw(canvas)
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

            surfaceTexture.updateTexture()
            blurPass(renderTextureHorizontal, gauss2PassHorizontal, false, surfaceTexture.getTextureID())
            blurPass(renderTextureVertical, gauss2PassVertical, true, renderTextureHorizontal.fboTex)
            renderFullscreenTexture()

    }

    fun renderFullscreenTexture() {
        setupViewPort(width, height, paddingTop)
        fullscreenTextureShader.useProgram()
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(fullscreenTextureShader.program, ShaderProgram.U_MATRIX), 1, false, projectionMatrixOrtho, 0)

        GLES20.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, renderTextureVertical.fboTex)
        GLES20.glUniform1i(GLES20.glGetUniformLocation(fullscreenTextureShader.program, "u_TextureUnit"), 0)
        spriteMesh.bindData(fullscreenTextureShader)
        spriteMesh.draw()
    }


    fun blurPass(renderTexture: RenderTexture, blurShader: TextureShaderProgram, isVerticalPass: Boolean, bindTextureID: Int) {
        setupViewPort((width * scale).toInt(), ((height + paddingTop) * scale).toInt(), 0f)
        renderTexture.bindRenderTexture()
        blurShader.useProgram()

        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(blurShader.program, ShaderProgram.U_MATRIX), 1, false, projectionMatrixOrtho, 0)

        GLES20.glActiveTexture(GLES30.GL_TEXTURE0)
        if (bindTextureID != surfaceTexture.getTextureID()) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, bindTextureID)
        } else {
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, bindTextureID)
        }
        GLES20.glUniform1i(GLES20.glGetUniformLocation(blurShader.program, "u_TextureUnit"), 0)

        if (isVerticalPass) {
            GLES20.glUniform1f(GLES20.glGetUniformLocation(blurShader.program, "uWidthOffset"), 1f / width.toFloat() / scale)
            GLES20.glUniform1f(GLES20.glGetUniformLocation(blurShader.program, "uHeightOffset"), 0f)
        } else {
            GLES20.glUniform1f(GLES20.glGetUniformLocation(blurShader.program, "uWidthOffset"), 0f)
            GLES20.glUniform1f(GLES20.glGetUniformLocation(blurShader.program, "uHeightOffset"), 1f / height.toFloat() / scale)
        }

        GLES20.glUniform1f(GLES20.glGetUniformLocation(blurShader.program, "scale"), scale)
        GLES20.glUniform1i(GLES20.glGetUniformLocation(blurShader.program, "blurRadius"), (blurRadius*scale).toInt())

        spriteMesh.bindData(blurShader)
        spriteMesh.draw()
        renderTexture.unbindRenderTexture()
    }
}