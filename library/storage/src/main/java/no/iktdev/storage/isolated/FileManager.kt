package no.iktdev.storage.isolated

import android.content.Context
import java.io.File

class FileManager(context: Context): Storage(context = context) {

    fun deleteFile(location: StorageLocation = StorageLocation.INTERNAL, path: Array<String>) {
        val file = getStorageFile(location, path)
        if (Exists(file))
            Delete(file)
    }

    fun file(location: StorageLocation = StorageLocation.INTERNAL, path: Array<String>): File? {
        val file = getStorageFile(location , path)
        return if (file.exists()) file else null
    }

}