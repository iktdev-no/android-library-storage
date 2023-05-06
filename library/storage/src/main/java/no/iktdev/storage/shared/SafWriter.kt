package no.iktdev.storage.shared

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.FileOutputStream
import java.lang.RuntimeException

class SafWriter(override val context: Context, val allowedUri: Uri, override val safRequestCode: Int = 1): Saf(context, safRequestCode) {
    init {
        if (allowedUri.path == null) {
            throw RuntimeException("Cannot perform access on invalid path!")
        }
    }

    fun write(data: ByteArray) {
        val descriptor: ParcelFileDescriptor = context.contentResolver.openFileDescriptor(allowedUri, "w") ?:
        throw RuntimeException("Could not obtain content resolver on: ${allowedUri.path}")
        val output: FileOutputStream = FileOutputStream(descriptor.fileDescriptor)
        output.write(data)
        output.close()
        descriptor.close()
    }
}