package no.iktdev.storage.shared

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.lang.RuntimeException
import java.util.UUID

open class Saf(open val context: Context) {


    fun safRequest(fileName: String, dataType: String): Intent {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = dataType
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
        return intent
    }

    fun writeFrom(permittedUri: Uri, data: ByteArray) {
        val descriptor: ParcelFileDescriptor = context.contentResolver.openFileDescriptor(permittedUri, "w") ?:
        throw RuntimeException("Could not obtain content resolver on: ${permittedUri.path}")
        val output: FileOutputStream = FileOutputStream(descriptor.fileDescriptor)
        output.write(data)
        output.close()
        descriptor.close()
    }

    fun readAndCache(permittedUri: Uri): File? {
        try {
            val fileName = context.contentResolver.query(permittedUri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            }
            val extension = if (fileName.isNullOrEmpty()) "" else File(fileName).extension
            val outFile = File.createTempFile(fileName ?: "cached${UUID.randomUUID()}", extension)
            return readInto(permittedUri, outFile)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun readInto(permittedUri: Uri, out: File): File? {
        try {
            context.contentResolver.openInputStream(permittedUri)?.use { inputStream ->
                val outStream = out.outputStream()
                inputStream.copyTo(outStream, bufferSize = DEFAULT_BUFFER_SIZE)
                outStream.close()
            }
            return out
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

}