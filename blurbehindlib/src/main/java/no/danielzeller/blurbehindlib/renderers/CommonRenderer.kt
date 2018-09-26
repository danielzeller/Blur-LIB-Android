package no.danielzeller.blurbehindlib.renderers

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.Matrix
import no.danielzeller.blurbehindlib.*
import no.opengl.danielzeller.opengltesting.opengl.gameobject.RenderTexture

class CommonRenderer (private val context: Context, internal val scale: Float){

    private val projectionMatrixOrtho = FloatArray(16)
    private lateinit var spriteMesh: SpriteMesh
    private val fullscreenTextureShader = TextureShaderProgram(R.raw.vertex_shader, R.raw.texture_fragment_shader)

    private val stackHorizontal = TextureShaderProgram(R.raw.vertex_shader, R.raw.stack_2_pass_horizontal)
    private val stackVertical = TextureShaderProgram(R.raw.vertex_shader, R.raw.stack_2_pass_vertical)

    private val boxHorizontal = TextureShaderProgram(R.raw.vertex_shader, R.raw.box_2_pass_horizontal)
    private val boxVertical = TextureShaderProgram(R.raw.vertex_shader, R.raw.box_2_pass_vertical)

    private val gauss2PassHorizontal = TextureShaderProgram(R.raw.vertex_shader, R.raw.gauss_2_pass_horizontal)
    private val gauss2PassVertical = TextureShaderProgram(R.raw.vertex_shader, R.raw.gauss_2_pass_vertical)

    private val gauss1PassNoised = TextureShaderProgram(R.raw.vertex_shader, R.raw.gauss_one_pass_noised)

    var surfaceTexture = ViewSurfaceTexture()
    var isCreated = false

    private var width = 0
    private var height = 0
    private var renderTextureHorizontal = RenderTexture()
    private var renderTextureVertical = RenderTexture()

    var blurRadius = 50f
    var blurMode = BlurMode.GAUSS_2_PASS

     fun onSurfaceCreated() {

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

        spriteMesh = SpriteMesh()
        fullscreenTextureShader.load(context)

        stackHorizontal.load(context)
        stackVertical.load(context)

        boxHorizontal.load(context)
        boxVertical.load(context)

        gauss2PassHorizontal.load(context)
        gauss2PassVertical.load(context)

        gauss1PassNoised.load(context)

    }

    fun onSurfaceChanged( width: Int, height: Int) {
        this.width = width
        this.height = height

        surfaceTexture.createSurface((width * scale).toInt(), (height * scale).toInt())
        renderTextureHorizontal.InitiateFrameBuffer((width * scale).toInt(), (height * scale).toInt())
        renderTextureVertical.InitiateFrameBuffer((width * scale).toInt(), (height * scale).toInt())
        clearViewSurfaceTexture()
        isCreated = true
    }


    private fun clearViewSurfaceTexture() {
        val canvas = surfaceTexture.beginDraw()
        canvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        surfaceTexture.endDraw(canvas)
    }

    private fun setupViewPort(width: Int, height: Int) {
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

    internal fun onDrawFrame() {
        surfaceTexture.updateTexture()

        if (blurMode == BlurMode.BOX) {
            blurPass(renderTextureHorizontal, boxHorizontal, false, surfaceTexture.getTextureID())
            blurPass(renderTextureVertical, boxVertical, true, renderTextureHorizontal.fboTex)
        } else if (blurMode == BlurMode.STACK) {
            blurPass(renderTextureHorizontal, stackHorizontal, false, surfaceTexture.getTextureID())
            blurPass(renderTextureVertical, stackVertical, true, renderTextureHorizontal.fboTex)
        } else if (blurMode == BlurMode.GAUSS_2_PASS) {
            blurPass(renderTextureHorizontal, gauss2PassHorizontal, false, surfaceTexture.getTextureID())
            blurPass(renderTextureVertical, gauss2PassVertical, true, renderTextureHorizontal.fboTex)


        } else {
            blurPass(renderTextureVertical, gauss1PassNoised, false, surfaceTexture.getTextureID())
        }

        renderFullscreenTexture()
    }

    fun renderFullscreenTexture() {
        setupViewPort(width, height)
        fullscreenTextureShader.useProgram()
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(fullscreenTextureShader.program, ShaderProgram.U_MATRIX), 1, false, projectionMatrixOrtho, 0)

        GLES20.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, renderTextureVertical.fboTex)
        GLES20.glUniform1i(GLES20.glGetUniformLocation(fullscreenTextureShader.program, "u_TextureUnit"), 0)
        spriteMesh.bindData(fullscreenTextureShader)
        spriteMesh.draw()
    }


    fun blurPass(renderTexture: RenderTexture, blurShader: TextureShaderProgram, isVerticalPass: Boolean, bindTextureID: Int) {
        setupViewPort((width * scale).toInt(), (height * scale).toInt())
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

            if (blurMode != BlurMode.GAUSS_1_PASS) {
                GLES20.glUniform1f(GLES20.glGetUniformLocation(blurShader.program, "uWidthOffset"), 0f)
                GLES20.glUniform1f(GLES20.glGetUniformLocation(blurShader.program, "uHeightOffset"), 1f / height.toFloat() / scale)
            }
        }

        GLES20.glUniform1f(GLES20.glGetUniformLocation(blurShader.program, "scale"), scale)

        GLES20.glUniform1i(GLES20.glGetUniformLocation(blurShader.program, "blurRadius"), blurRadius.toInt())

        spriteMesh.bindData(blurShader)
        spriteMesh.draw()
        renderTexture.unbindRenderTexture()
    }
}