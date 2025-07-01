package no.iktdev.storage.appstore

import java.io.File

data class StoreHolder(
    val id: String,
    val type: StoreType = StoreType.Any,
    val file: File
)


enum class StoreType {
    Any,
    Internal,
    SDCard,
    External
}