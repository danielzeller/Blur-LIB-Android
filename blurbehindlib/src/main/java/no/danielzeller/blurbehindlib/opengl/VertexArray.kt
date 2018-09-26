package no.danielzeller.blurbehindlib


import android.opengl.GLES20.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class VertexArray(vertexData: FloatArray) {

    private val floatBuffer: FloatBuffer
    private val BYTES_PER_FLOAT = 4

    init {
        floatBuffer = ByteBuffer
                .allocateDirect(vertexData.size * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData)
    }

    fun setVertexAttribPointer(dataOffset: Int, attributeLocation: Int, componentCount: Int, stride: Int) {

        floatBuffer.position(dataOffset)
        glVertexAttribPointer(attributeLocation, componentCount, GL_FLOAT,
                false, stride, floatBuffer)
        glEnableVertexAttribArray(attributeLocation)

        floatBuffer.position(0)
    }
}
