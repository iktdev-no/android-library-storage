package no.iktdev.storage

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.storage.StorageManager
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.sync.Mutex
import no.iktdev.storage.isolated.Storage.Companion.fromJson
import java.io.File
import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import java.nio.file.Paths
import java.util.regex.Pattern
import kotlin.use

object StorageUtil {
    var logWarnIfReadingFileDoesNotExists = true


    fun internalAppStorage(context: Context): File = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        ContextWrapper(context).dataDir
    } else {
        ContextWrapper(context).filesDir.parentFile
    }

    fun externalAppStorage(context: Context): List<File> {
        val internalPath =
            "/" + internalAppStorage(context).absolutePath.split("/").filter { it.isNotBlank() }
                .take(3).joinToString("/") + "/"
        val filter = Pattern.compile("(emulated|self)")
        return ContextWrapper(context).getExternalFilesDirs(null)
            .filter { !it.startsWith(internalPath) }
            .filter { !filter.matcher(it.absolutePath).find() }.mapNotNull { it.parentFile }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun storageNameResolver(context: Context, file: File): String {
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        val volume = storageManager.getStorageVolume(file) ?: return context.getString(R.string.setting_managing_storage_internal)

        val desc = volume.getDescription(context) // F.eks. "SD-kort", "USB-lagring", "Intern lagring"
        val uuid = volume.uuid
        val isRemovable = volume.isRemovable
        val isPrimary = volume.isPrimary

        Log.d("Storage", "Beskrivelse: $desc, Avtagbar: $isRemovable, Primær: $isPrimary, UUID: $uuid")

        return desc
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @DrawableRes
    fun getIconForStorage(context: Context, file: File): Int {
        val englishContext = context.createConfigurationContext(context.resources.configuration.apply { setLocale(java.util.Locale.ENGLISH) })

        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        val volume = storageManager.getStorageVolume(file) ?: return R.drawable.outline_hard_disk_24

        val desc = volume.getDescription(englishContext) // Always in English
        if (desc.contains("SD", true)) {
            return R.drawable.baseline_sd_card_24
        }

        return R.drawable.outline_hard_disk_24
    }

}

fun File.with(vararg path: String): File {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Paths.get(this.path, *path).toFile()
    } else {
        var file = this
        for (p in path) {
            file = File(file, p)
        }
        return file
    }
}

fun File.getOnlyFiles(): List<File> {
    return this.walk().filter { it.isFile }.toList()
}

inline fun <reified T> File.readJson() : T? {
    if (!this.exists()) {
        if (StorageUtil.logWarnIfReadingFileDoesNotExists) {
            Log.w(this::class.java.simpleName, "File ${this.absolutePath} does not exist")
        }
        return null
    }
    val typeToken = object: TypeToken<T>() {}.type
    return fromJson(this.readText(), typeToken)
}

fun File.readOrNull(): String? {
    return try {
        if (this.exists()) {
            this.readText()
        } else null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private val fileMutex = Mutex()
suspend fun File.withLock(block: (File) -> Unit) {
    Log.d("StorageUtil", "Locking file: ${this.absolutePath}")
    fileMutex.lock()
    try {
        Log.d("StorageUtil", "Acquired lock for file: ${this.absolutePath}")
        RandomAccessFile(this, "rw").use { raf ->
            val channel: FileChannel = raf.channel
            channel.lock().use { _ ->
                block(this)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        fileMutex.unlock()
        Log.d("StorageUtil", "Released lock for file: ${this.absolutePath}")
    }
}

fun File.writeJson(data: Any) {
    val json = Gson().toJson(data)
    if (!this.exists()) {
        this.parentFile?.mkdirs()
        val created = this.createNewFile()
        if (!created) {
            throw IllegalStateException("Failed to create file: ${this.absolutePath}")
        }
    }
    this.writeText(json)
}
