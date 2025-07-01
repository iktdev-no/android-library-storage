package no.iktdev.storage.appstore

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import no.iktdev.storage.isolated.excpetion.StorageException
import java.io.File
import java.util.regex.Pattern

open class Store(val context: Context) {

    fun isExternalStorageAvailable(): Boolean {
        return try {
            getSDCardAppStoreFolder().any { it.exists() }
        } catch (e: StorageException) {
            false
        }
    }

    fun getDeviceAppStoreFolder(): File {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ContextWrapper(context).dataDir
        } else {
            ContextWrapper(context).filesDir.parentFile ?: ContextWrapper(context).obbDirs.first()
        }
    }

    fun getSDCardAppStoreFolder(): List<File> {
        val internalPath =
            "/" + getDeviceAppStoreFolder().absolutePath.split("/").filter { it.isNotBlank() }
                .take(3).joinToString("/") + "/"
        val filter = Pattern.compile("(emulated|self)")
        return ContextWrapper(context).getExternalFilesDirs(null)
            .filter { !it.startsWith(internalPath) }
            .filter { !filter.matcher(it.absolutePath).find() }.mapNotNull { it.parentFile }
    }

    fun getStores(): List<StoreHolder> {
        val items = mutableListOf<StoreHolder>()
        getDeviceAppStoreFolder().let {
            items.add(StoreHolder(it.hashCode().toString(), StoreType.Internal, it))
        }
        getSDCardAppStoreFolder().forEach {
            items.add(StoreHolder(it.hashCode().toString(), StoreType.SDCard, it))
        }

        return items
    }


}