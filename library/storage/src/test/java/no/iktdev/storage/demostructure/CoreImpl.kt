package no.iktdev.storage.demostructure

data class CoreImpl<T>(override val header: String, var data: T?): CoreData(header)
