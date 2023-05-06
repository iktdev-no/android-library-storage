package no.iktdev.storage.isolated

import android.content.Context
import com.google.gson.reflect.TypeToken

abstract class Reader(context: Context) : Storage(context) {

    inline fun <reified T> readWith(path: Array<String>, location: StorageLocation = StorageLocation.INTERNAL) : T? {
        val typeToken = object: TypeToken<T>() {}.type
        val data = readWith(path, location) ?: return null
        return fromJson(data, typeToken)
    }

    fun readWith(path: Array<String>, location: StorageLocation = StorageLocation.INTERNAL): String? {
        val file = getStorageFile(location, path)
        if (!file.exists())
            return null
        return Read(file)
    }

}