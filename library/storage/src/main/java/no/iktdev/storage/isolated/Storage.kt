package no.iktdev.storage.isolated

import android.content.Context
import android.content.ContextWrapper
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import no.iktdev.storage.isolated.excpetion.StorageException
import java.io.*
import java.lang.reflect.Type
import java.util.regex.Pattern
import kotlin.jvm.Throws


abstract class Storage(val context: Context)
{
    companion object {

        fun getInternalFolder(context: Context): File
        {
            return Storage.Internal(context).data
        }

        @Throws(StorageException::class)
        fun getExternalFolder(context: Context): File
        {
            return External(context).data!! // Its always not null, if not exception above will be thrown
        }

        fun isExternalStorageAvailable(context: Context): Boolean {
            return try {
                External(context).root().exists()
            } catch (e: StorageException) {
                false
            }
        }

        inline fun <reified T> fromJson(json: String, type: Type): T?
        {
            try {
                return Gson().fromJson<T>(json, type)
            } catch (e: Exception) {
                Log.e(this::class.java.simpleName, "Library error ${e.message}")
                e.printStackTrace()
            }
            return null
        }
    }

    inline fun <reified T> isStringType(): Boolean {
        return (object: TypeToken<T>() {}.type == object: TypeToken<String>() {}.type)
    }

    /**
     * @return true if exists or file + path created
     */
    @Suppress("canBePrivate")
    fun File.createFileIfNotExists(): Boolean {
        val parent = this.parentFile ?: return false
        if (!parent.createDirectoriesIfNotExists()) return false
        return if (!this.exists()) this.createNewFile() else true
    }

    /**
     * Creates directories if not exists
     * @return true if exists or created
     */
    @Suppress("canBePrivate")
    fun File.createDirectoriesIfNotExists(): Boolean {
        return if (this.exists() && this.isDirectory) true else this.mkdirs()
    }

    fun File.addTo(part: String): File {
        return File(this, part)
    }


    /**
     * This method will get a file based on the path
     * If full path does not exists, folders will be created
     * @param location INTERNAL || EXTERNAL
     * @param path path in string array
     */
    fun getStorageFile(location: StorageLocation, path: Array<String>): File {
        var file: File = if (location == StorageLocation.EXTERNAL && isExternalStorageAvailable(context)) getExternalFolder(context) else getInternalFolder(context)
        path.forEach { part -> file = File(file, part) }
        return file
    }

    fun getStorageFile(absolutePath: String): File? {
        val file: File = File(absolutePath)
        return if (file.exists()) file else null
    }


    fun Write(file: File, data: ByteArray) {
        file.setWritable(true)
        val fos = FileOutputStream(file)
        fos.write(data)
        fos.close()
    }

    fun Write(file: File, data: String) {
        val byteArray = data.toByteArray(Charsets.UTF_8)
        Write(file, byteArray)
    }


    fun Read(file: File): String?
    {
        if (!file.isFile) {
            return null
        }

        val inputStream = FileInputStream(file)
        val reader = BufferedReader(InputStreamReader(inputStream));
        val builder: StringBuilder = StringBuilder();

        var line: String?
        while (reader.readLine().also { line = it } != null)
        {
            builder.appendLine(line)
        }
        inputStream.close()
        reader.close()
        return builder.toString().filter { !it.isISOControl() }
    }


    fun Delete(target: File) {
        target.delete()
    }

    fun Exists(target: File): Boolean {
        return (target.exists())
    }

    fun allFiles(target: File): List<File> {
        return target.walk().filter {
            it.isFile
        }.toList()
    }
    fun getDirectoriesOf(target: File): List<File> {
        return target.listFiles { it -> it.isDirectory }?.toList() ?: emptyList()
    }


    abstract class Repository(context: Context) {
        abstract fun root(): File
        fun files(): List<File> {
            return root().listFiles()?.filterNotNull() ?: emptyList()
        }

        /**
         * @param uri.path
         */
        fun readable(path: String): Boolean {
            val file = File(path)
            return file.exists() && file.canRead()
        }

        /**
         * @param uri.path
         */
        fun writable(path: String): Boolean {
            val file = File(path)
            return file.exists() && file.canWrite() || file.parentFile != null && file.parentFile?.canWrite() ?: false
        }

    }

    class Internal(context: Context): Repository(context) {
        var data: File
        init {
            data = getDirectory(context)
        }

        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        fun getDirectory(context: Context): File {
            val storageDirectory = ContextWrapper(context).filesDir
            if (storageDirectory.parentFile != null) {
                return storageDirectory.parentFile
            }
            return storageDirectory
        }

        override fun root(): File { return Environment.getDataDirectory() }
    }

    class External(context: Context): Repository(context) {
        var data: File? = null
        private val filter = Pattern.compile("(emulated|self)")
        init {
            val files = ContextCompat.getExternalFilesDirs(context, null)
            files.forEach { file: File ->
                if (!filter.matcher(file.absolutePath).find()) {
                    if (file.parentFile != null) {
                        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                        data = file.parentFile
                    } else {
                        data = file
                    }
                }
            }
            if (data == null) {
                throw StorageException("No actual external storage present..")
            }
        }
        override fun root(): File {
            if (data == null) {
                throw StorageException("No actual external storage present..")
            }
            var path = data!!.absolutePath
            path = path.substring(0, path.indexOf("data"))
            return File(path)
        }
    }

}