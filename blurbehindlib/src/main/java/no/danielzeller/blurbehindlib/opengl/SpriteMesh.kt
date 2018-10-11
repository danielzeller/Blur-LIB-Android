package no.danielzeller.blurbehindlib.opengl

import android.opengl.GLES20.GL_TRIANGLE_FAN
import android.opengl.GLES20.glDrawArrays

class SpriteMesh {

    private var vertexArray: VertexArray

    init {
        vertexArray = VertexArray(VERTEX_DATA)
    }

    fun bindData(textureProgram: ShaderProgram) {

        vertexArray.setVertexAttribPointer(
                0,
                textureProgram.positionAttributeLocation,
                POSITION_COMPONENT_COUNT,
                STRIDE)

        vertexArray.setVertexAttribPointer(
                POSITION_COMPONENT_COUNT,
                textureProgram.textureCoordinatesAttributeLocation,
                TEXTURE_COORDINATES_COMPONENT_COUNT,
                STRIDE)
    }

    fun draw() {
        glDrawArrays(GL_TRIANGLE_FAN, 0, 6)
    }

    companion object {

        private const val BYTES_PER_FLOAT = 4
        private const val POSITION_COMPONENT_COUNT = 2
        private const val TEXTURE_COORDINATES_COMPONENT_COUNT = 2
        private const val STRIDE = (POSITION_COMPONENT_COUNT + TEXTURE_COORDINATES_COMPONENT_COUNT) * BYTES_PER_FLOAT


        val VERTEX_DATA = floatArrayOf(
                // Order of coordinates: X, Y, S, T
                // Triangle Fan
                0.0f, 0.0f, 0.5f, 0.5f,
                -1.0f, -1.0f, 0.0f, 1.0f,
                1.0f, -1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f, 0.0f,
                -1.0f, 1.0f, 0.0f, 0.0f,
                -1.0f, -1.0f, 0.0f, 1.0f)
    }
}
