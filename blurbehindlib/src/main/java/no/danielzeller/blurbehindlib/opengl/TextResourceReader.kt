package no.danielzeller.blurbehindlib.opengl

import android.content.Context
import android.content.res.Resources

import java.io.IOException

object TextResourceReader {
    /**
     * Reads in text from a resource file and returns a String containing the
     * text.
     */
    fun readTextFileFromResource(context: Context, resourceId: Int): String {

        val body = StringBuilder()

        try {
            val bufferedReader = context.resources.openRawResource(resourceId).bufferedReader()

            bufferedReader.use {

                body.append(it.readText())
                body.append("\n")
            }
        } catch (e: IOException) {
            throw RuntimeException("Could not open resource: $resourceId", e)
        } catch (nfe: Resources.NotFoundException) {
            throw RuntimeException("Resource not found: $resourceId", nfe)
        }

        return body.toString()
    }
}
