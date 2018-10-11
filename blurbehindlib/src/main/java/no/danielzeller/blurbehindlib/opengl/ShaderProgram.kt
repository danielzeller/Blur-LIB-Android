package no.danielzeller.blurbehindlib.opengl
import android.content.Context
import android.opengl.GLES20.glUseProgram

abstract class ShaderProgram(private var vertexShaderResourceId: Int, private var fragmentShaderResourceId: Int) {

    private var isLoaded: Boolean = false

    var program: Int = 0
        protected set

    var positionAttributeLocation: Int = 0

    var textureCoordinatesAttributeLocation: Int = 0


    open fun load(context: Context) {

        program = ShaderHelper.buildProgram(
                TextResourceReader.readTextFileFromResource(
                        context, vertexShaderResourceId),
                TextResourceReader.readTextFileFromResource(
                        context, fragmentShaderResourceId))
        isLoaded = true
    }

    fun useProgram() {
        glUseProgram(program)
    }

    companion object {
        const val U_MATRIX = "u_Matrix"
        const val U_TEXTURE_UNIT = "surface_texture"

        const val A_POSITION = "a_Position"
        const val A_TEXTURE_COORDINATES = "a_TextureCoordinates"
        const val A_CUTOFF_LOCATION = "cutoff"
    }
}
