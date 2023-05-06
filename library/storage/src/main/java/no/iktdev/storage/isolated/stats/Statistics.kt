package no.iktdev.storage.isolated.stats

import android.os.StatFs
import no.iktdev.storage.isolated.Storage
import java.io.File

class Statistics(val root: File) {

    fun allFiles(data: File): List<File> {
        return data.walk().filter {
            it.isFile
        }.toList()
    }

    /**
     *
     * @param data File object with reference to data-folder
     * @return total size left on storage
     */
    fun getAvailableMemory(data: File): Long {
        val stats = StatFs(data.path)
        val blockSize = stats.blockSizeLong
        val availableBlocks = stats.availableBlocksLong
        return availableBlocks * blockSize
    }

    /**
     *
     * @param data File object with reference to data-folder
     * @return total size of storage
     */
    fun getTotalMemory(data: File): Long {
        val stats = StatFs(data.path)
        val blockSize = stats.blockSizeLong
        val totalBlocks = stats.blockCountLong
        return totalBlocks * blockSize
    }

    fun getFolderUsage(folder: File): Long {
        var size: Long = 0
        if (!folder.exists()) return size
        val files: List<File> = allFiles(folder)
        for (file in files) {
            size += file.length()
        }
        return size
    }

    /**
     * Calculates size occupied by other apps and content than StreamIT
     * @param data Data path
     * @param content Content path to StreamIT
     * @return Occupied size by other content than StreamIT
     */
    fun getOccupiedUsage(data: File, content: File): Long {
        return getTotalMemory(data) - getAvailableMemory(data) - getFolderUsage(content)
    }

    /**
     * Calculates size of storage used on storage device
     * @param data Data path
     * @return Size used on device
     */
    fun getUsedMemory(data: File): Long {
        return getTotalMemory(data) - getAvailableMemory(data)
    }
}