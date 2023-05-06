package no.iktdev.storage.isolated

import android.content.Context
import android.location.Location
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset

abstract class Writer(context: Context) : Storage(context) {

    /**
     * Use type Any if you want to skip Gson encoding, and do this later
     * If path does not exists, path will be created based on the path
     * @param path Full path to file
     */
    fun writeWith(data: Any, path: Array<String>, location: StorageLocation = StorageLocation.INTERNAL)
    {
        val file = getStorageFile(location, path)
        when (data) {
            is ByteArray -> {
                Write(file, data)
            }
            is String -> {
                val byted = data.toByteArray(Charsets.UTF_8)
                Write(file, byted)
            }
            else -> {
                val enc = Gson().toJson(data)
                Write(file, enc)
            }
        }
    }
}