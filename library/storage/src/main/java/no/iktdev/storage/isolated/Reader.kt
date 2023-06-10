package no.iktdev.storage.isolated

import android.content.Context
import android.util.Log
import com.google.gson.reflect.TypeToken
import java.io.File

abstract class Reader(context: Context) : Storage(context) {

    inline fun <reified T> readWith(path: Array<String>, location: StorageLocation = StorageLocation.INTERNAL) : T? {
        val typeToken = object: TypeToken<T>() {}.type
        val data = readString(path, location) ?: return null
        return fromJson(data, typeToken)
    }

    inline fun <reified T> readWith(file: File,) : T? {
        val typeToken = object: TypeToken<T>() {}.type
        val data = readString(file) ?: return null
        return fromJson(data, typeToken)
    }

    fun readString(path: Array<String>, location: StorageLocation = StorageLocation.INTERNAL): String? {
        val file = getStorageFile(location, path)
        if (!file.exists()) {
            Log.e(this::class.java.simpleName, "Library - File ${file.absolutePath} could not be found")
            return null
        }
        return Read(file)
    }

    fun readString(file: File, location: StorageLocation = StorageLocation.INTERNAL): String? {
        if (!file.exists()) {
            Log.e(this::class.java.simpleName, "Library - File ${file.absolutePath} could not be found")
            return null
        }
        return Read(file)
    }

}