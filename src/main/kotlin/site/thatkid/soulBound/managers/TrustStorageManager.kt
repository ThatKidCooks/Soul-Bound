package site.thatkid.soulBound.managers

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import site.thatkid.soulBound.hearts.TrustRegistry
import java.io.File
import java.lang.reflect.Type
import java.util.*

object TrustStorageManager {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val type: Type = object : TypeToken<Map<UUID, Set<UUID>>>() {}.type

    fun save(file: File) {
        val combinedMap = TrustRegistry.trustedPlayers

        // Serialize to file
        file.writer().use {
            gson.toJson(combinedMap, type, it)
        }
    }

    fun load(file: File) {
        if (!file.exists()) return

        val map: Map<UUID, Set<UUID>> = file.reader().use {
            gson.fromJson(it, type)
        } ?: return

        // Distribute loaded data to all hearts
        TrustRegistry.trustedPlayers.clear()
        map.forEach { (owner, trustedSet) ->
            TrustRegistry.trustedPlayers[owner] = trustedSet.toMutableSet()
        }

    }
}
