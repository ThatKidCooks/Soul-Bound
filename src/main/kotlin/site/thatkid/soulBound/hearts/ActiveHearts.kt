package site.thatkid.soulBound.hearts

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import site.thatkid.soulBound.items.Heart
import site.thatkid.soulBound.json.SavedHeartData
import java.io.File
import java.util.*

object ActiveHearts {

    enum class AddHeartResult {
        SUCCESS,
        SAME_HEART,
        COOLDOWN_ACTIVE
    }

    private var done: Boolean = false

    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    private val playerHearts: MutableMap<UUID, Heart> = mutableMapOf()

    fun add(playerUUID: UUID, heart: Heart): AddHeartResult {
        val currentHeart = playerHearts[playerUUID]

        if (currentHeart != null && currentHeart::class == heart::class) {
            return AddHeartResult.SAME_HEART
        }

        currentHeart?.getCooldown(playerUUID)?.let { cooldown ->
            if (cooldown > 0L) {
                return AddHeartResult.COOLDOWN_ACTIVE
            }
        }

        if (currentHeart != null) {
            val player = Bukkit.getPlayer(playerUUID)
            player?.inventory?.addItem(currentHeart.createItem())
        }

        playerHearts[playerUUID] = heart
        return AddHeartResult.SUCCESS
    }



    fun remove(player: Player, slot: Int) : Heart {
        val playerUUID = player.uniqueId

        val heart = when (slot) {
            1 -> playerHearts.remove(playerUUID)
            else -> null
        }

        if (heart != null) {
            player.inventory.addItem(heart.createItem())
        }

        return heart ?: throw IllegalArgumentException("No heart found in slot $slot for player ${player.name} (${playerUUID})")
    }

    // Alternative remove method that doesn't give the item back
    fun removeWithoutItem(playerUUID: UUID) {
        playerHearts.remove(playerUUID)
    }

    fun removeHeart(player: Player, heart: Heart) {
        val playerUUID = player.uniqueId
        val currentHeart = playerHearts[playerUUID]

        if (currentHeart != null && currentHeart::class == heart::class) {
            playerHearts.remove(playerUUID)
        } else {
            Bukkit.getLogger().warning("No matching heart found for player ${player.name} (${playerUUID})")
        }
    }

    fun getHearts(playerUUID: UUID): List<Heart> {
        val heart = playerHearts[playerUUID]
        return if (heart != null) listOf(heart) else emptyList()
    }

    fun saveToFile(file: File) {
        val toSave = playerHearts.map { (uuid, heart) ->
            SavedHeartData(uuid, listOf(heart::class.qualifiedName!!))
        }

        file.writeText(gson.toJson(toSave))
    }

    fun loadFromFile(file: File) {
        if (!file.exists()) return

        val content = file.readText()
        val list = gson.fromJson(content, Array<SavedHeartData>::class.java)

        for (entry in list) {
            val className = entry.hearts.firstOrNull()
            if (className != null) {
                try {
                    val clazz = Class.forName(className).kotlin
                    val heart = clazz.objectInstance as? Heart
                    if (heart != null) {
                        playerHearts[entry.uuid] = heart
                    } else {
                        Bukkit.getLogger().warning("Could not load heart class $className — not a singleton object.")
                    }
                } catch (e: Exception) {
                    Bukkit.getLogger().warning("Failed to load heart class $className: ${e.message}")
                }
            }
        }
    }
} 