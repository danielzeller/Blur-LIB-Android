package no.danielzeller.blurbehindlib.opengl

import android.opengl.GLES20.*
import android.util.Log

object ShaderHelper {

    private const val TAG = "ShaderHelper"

    private fun compileVertexShader(shaderCode: String): Int {
        return compileShader(GL_VERTEX_SHADER, shaderCode)
    }

    private fun compileFragmentShader(shaderCode: String): Int {
        return compileShader(GL_FRAGMENT_SHADER, shaderCode)
    }

    private fun compileShader(type: Int, shaderCode: String): Int {

        val shaderObjectId = glCreateShader(type)

        if (shaderObjectId == 0) {

            Log.w(TAG, "Could not create new shader.")

            return 0
        }

        glShaderSource(shaderObjectId, shaderCode)

        glCompileShader(shaderObjectId)

        val compileStatus = IntArray(1)
        glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileStatus, 0)

        Log.v(TAG, "Results of compiling source:" + "\n" + shaderCode
                + "\n:" + glGetShaderInfoLog(shaderObjectId))

        // Verify the compile status.
        if (compileStatus[0] == 0) {
            // If it failed, delete the shader object.
            glDeleteShader(shaderObjectId)

            Log.w(TAG, "Compilation of shader failed.")

            return 0
        }

        // Return the shader object ID.
        return shaderObjectId
    }

    /**
     * Links a vertex shader and a fragment shader together into an OpenGL
     * program. Returns the OpenGL program object ID, or 0 if linking failed.
     */
    private fun linkProgram(vertexShaderId: Int, fragmentShaderId: Int): Int {

        // Create a new program object.
        val programObjectId = glCreateProgram()

        if (programObjectId == 0) {
            Log.e(TAG, "Could not create new program")

            return 0
        }

        glAttachShader(programObjectId, vertexShaderId)

        glAttachShader(programObjectId, fragmentShaderId)

        glLinkProgram(programObjectId)

        val linkStatus = IntArray(1)
        glGetProgramiv(programObjectId, GL_LINK_STATUS,
                linkStatus, 0)


        if (linkStatus[0] == 0) {
            glDeleteProgram(programObjectId)

            Log.e(TAG, "Linking of program failed.")

            return 0
        }
        return programObjectId
    }

    /**
     * Validates an OpenGL program. Should only be called when developing the
     * application.
     */
    private fun validateProgram(programObjectId: Int): Boolean {

        glValidateProgram(programObjectId)
        val validateStatus = IntArray(1)
        glGetProgramiv(programObjectId, GL_VALIDATE_STATUS, validateStatus, 0)

        return validateStatus[0] != 0
    }

    fun buildProgram(vertexShaderSource: String,   fragmentShaderSource: String): Int {

        val program: Int

        // Compile the shaders.
        val vertexShader = compileVertexShader(vertexShaderSource)
        val fragmentShader = compileFragmentShader(fragmentShaderSource)

        // Link them into a shader program.
        program = linkProgram(vertexShader, fragmentShader)

        validateProgram(program)

        return program
    }
}
