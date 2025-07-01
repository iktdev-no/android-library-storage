package no.iktdev.storage.appstore

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.lang.reflect.Type

@Suppress("Unused")
fun File.read(): String? {
    if (!this.isFile || !this.canRead()) {
        return null
    }
    val builder: StringBuilder = StringBuilder()
    var inputStream: FileInputStream? = null
    var reader: BufferedReader? = null

    try {
        inputStream = FileInputStream(this)
        reader = BufferedReader(InputStreamReader(inputStream));

        var line: String?
        while (reader.readLine().also { line = it } != null)
        {
            builder.appendLine(line)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        inputStream?.close()
        reader?.close()
    }

    return builder.toString().filter { !it.isISOControl() }
}

/**
 * Uses GSON, and its required to be imported in your project
 */
inline fun <reified T> String.fromJson(type: Type): T? {
    try {
        return Gson().fromJson<T>(this, type)
    } catch (e: Exception) {
        Log.e(this::class.java.simpleName, "Library error ${e.message}")
        e.printStackTrace()
    }
    return null
}

/**
 * Uses GSON, and its required to be imported in your project
 */
inline fun <reified T> String.fromJson(): T? {
    try {
        val typeToken = object: TypeToken<T>() {}.type
        return Gson().fromJson<T>(this, typeToken)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

/**
 * @return List of files (directories are filtered out)
 */
fun File.getAllFiles(): List<File> {
    return this.walk().filter { it.isFile }.toList()
}

fun File.write(data: String) {
    this.write(data.toByteArray(Charsets.UTF_8))
}

fun File.ifExists(block: () -> Unit) {
    if (this.exists()) {
        block()
    }
}

fun File.write(data: ByteArray) {
    this.createIfNotExists()
    this.setWritable(true)
    var outStream: FileOutputStream? = null
    try {
        outStream = FileOutputStream(this)
        outStream.write(data)
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        outStream?.close()
    }
}

fun File.createIfNotExists(): File? {
    return try {
        if (this.parentFile?.exists() == false) {
            this.parentFile?.mkdirs()
        }

        if (this.isFile && !this.exists()) {
            this.createNewFile()
        } else if (this.isDirectory && !this.exists()) {
            this.mkdirs()
        }
        this
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun File.withPath(vararg path: String): File {
    var file = this
    path.toList().forEach { file = file.resolve(it) }
    return file
}
