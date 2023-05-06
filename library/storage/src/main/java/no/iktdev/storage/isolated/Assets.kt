package no.iktdev.storage.isolated

import android.content.Context
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

open class Assets() {

    @Throws(IOException::class)
    fun readFile(context: Context, fileName: String): String {
        val stream: InputStream = context.assets.open(fileName)
        val reader = BufferedReader(InputStreamReader(stream));
        val builder: StringBuilder = StringBuilder();

        var line: String?
        while (reader.readLine().also { line = it } != null)
        {
            builder.appendLine(line)
        }
        stream.close()
        reader.close()
        return builder.toString()
    }

}