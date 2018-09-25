package no.danielzeller.blurbehindlib

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLES20.*
import android.opengl.GLES30
import android.opengl.GLSurfaceView.Renderer
import android.opengl.Matrix
import no.opengl.danielzeller.opengltesting.opengl.gameobject.RenderTexture
import no.opengl.danielzeller.opengltesting.opengl.util.TextureHelper
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

enum class BlurMode(value: Int) {
    BOX(1),
    STACK(2),
    GAUSS_2_PASS(3),
    GAUSS_1_PASS(4)
}

class GLSurfaceViewRenderer(private val context: Context, internal val scale: Float) : Renderer {

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
    private var renderTexture = RenderTexture()
    private var renderTextureVertical = RenderTexture()
    var blurRadius = 50f
    var noiseTextureID = -1;
    var blurMode = BlurMode.GAUSS_2_PASS

    override fun onSurfaceCreated(glUnused: GL10, config: EGLConfig) {

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

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

    override fun onSurfaceChanged(glUnused: GL10, width: Int, height: Int) {
        this.width = width
        this.height = height

        surfaceTexture.createSurface((width * scale).toInt(), (height * scale).toInt())
        renderTexture.InitiateFrameBuffer((width * scale).toInt(), (height * scale).toInt())
        renderTextureVertical.InitiateFrameBuffer((width * scale).toInt(), (height * scale).toInt())
        clearViewSurfaceTexture()
        isCreated = true
        noiseTextureID = TextureHelper.loadTexture(context, R.drawable.noise)
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


    private fun renderFullscreenRenderTexture() {
        surfaceTexture.updateTexture()

        if (blurMode == BlurMode.BOX) {
            blurPass(renderTexture, boxHorizontal, false, surfaceTexture.getTextureID())
            blurPass(renderTextureVertical, boxVertical, true, renderTexture.fboTex)
        } else if (blurMode == BlurMode.STACK) {
            blurPass(renderTexture, stackHorizontal, false, surfaceTexture.getTextureID())
            blurPass(renderTextureVertical, stackVertical, true, renderTexture.fboTex)
        } else if (blurMode == BlurMode.GAUSS_2_PASS) {
            blurPass(renderTexture, gauss2PassHorizontal, false, surfaceTexture.getTextureID())
            blurPass(renderTextureVertical, gauss2PassVertical, true, renderTexture.fboTex)
        } else {
            blurPass(renderTextureVertical, gauss1PassNoised, false, surfaceTexture.getTextureID())
        }

        renderFullscreenTexture()
    }

    fun renderFullscreenTexture() {
        setupViewPort(width, height)
        fullscreenTextureShader.useProgram()
        glUniformMatrix4fv(glGetUniformLocation(fullscreenTextureShader.program, ShaderProgram.U_MATRIX), 1, false, projectionMatrixOrtho, 0)

        glActiveTexture(GLES30.GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, renderTextureVertical.fboTex)
        glUniform1i(glGetUniformLocation(fullscreenTextureShader.program, "u_TextureUnit"), 0)
        spriteMesh.bindData(fullscreenTextureShader)
        spriteMesh.draw()
    }

    fun blurPass(renderTexture: RenderTexture, blurShader: TextureShaderProgram, isVerticalPass: Boolean, bindTextureID: Int) {
        setupViewPort((width * scale).toInt(), (height * scale).toInt())
        renderTexture.bindRenderTexture()
        blurShader.useProgram()
        glUniformMatrix4fv(glGetUniformLocation(blurShader.program, ShaderProgram.U_MATRIX), 1, false, projectionMatrixOrtho, 0)
        glActiveTexture(GLES30.GL_TEXTURE0)
        if (isVerticalPass) {
            glBindTexture(GL_TEXTURE_2D, bindTextureID)
            glUniform1f(glGetUniformLocation(blurShader.program, "uWidthOffset"), 1f / width.toFloat() / scale)
            glUniform1f(glGetUniformLocation(blurShader.program, "uHeightOffset"), 0f)
        } else {
            glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, bindTextureID)
            if (blurMode != BlurMode.GAUSS_1_PASS) {
                glUniform1f(glGetUniformLocation(blurShader.program, "uWidthOffset"), 0f)
                glUniform1f(glGetUniformLocation(blurShader.program, "uHeightOffset"), 1f / height.toFloat() / scale)
            }
        }
        glUniform1i(glGetUniformLocation(blurShader.program, "u_TextureUnit"), 0)
        glUniform1f(glGetUniformLocation(blurShader.program, "scale"), scale)

        glUniform1i(glGetUniformLocation(blurShader.program, "blurRadius"), blurRadius.toInt())

        spriteMesh.bindData(blurShader)
        spriteMesh.draw()
        renderTexture.unbindRenderTexture()
    }

    override fun onDrawFrame(glUnused: GL10) {
        glClear(GL_COLOR_BUFFER_BIT)
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        renderFullscreenRenderTexture()
    }
}
