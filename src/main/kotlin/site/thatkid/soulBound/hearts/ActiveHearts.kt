package site.thatkid.soulBound.hearts

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import site.thatkid.soulBound.items.Heart
import site.thatkid.soulBound.items.hearts.normal.Aquatic
import site.thatkid.soulBound.items.hearts.rare.Crowned
import site.thatkid.soulBound.items.hearts.normal.Fire
import site.thatkid.soulBound.items.hearts.normal.Frozen
import site.thatkid.soulBound.items.hearts.normal.Ghastly
import site.thatkid.soulBound.items.hearts.normal.Golem
import site.thatkid.soulBound.items.hearts.normal.Haste
import site.thatkid.soulBound.items.hearts.normal.Strength
import site.thatkid.soulBound.items.hearts.normal.Trader
import site.thatkid.soulBound.items.hearts.legendary.Warden
import site.thatkid.soulBound.items.hearts.rare.Wise
import site.thatkid.soulBound.items.hearts.legendary.Wither
import java.io.File
import java.util.*

/**
 * Manages the active hearts for all players on the server.
 *
 * This singleton object handles:
 * - Tracking which heart each player currently has active
 * - Adding and removing hearts with cooldown protection
 * - Persisting heart data to/from JSON files
 * - Providing lookup methods for other systems
 *
 * Key Rules:
 * - Players can only have one heart active at a time
 * - Switching hearts has a cooldown period to prevent abuse
 * - Heart data persists across server restarts
 */
object ActiveHearts {

    /** List of all available heart types in the plugin */
    private val allHearts: List<Heart> = listOf(
        Aquatic,
        Crowned,
        Fire,
        Frozen,
        Golem,
        Ghastly,
        Haste,
        Strength,
        Trader,
        Warden,
        Wither,
        Wise
    )

    /** Result codes for adding hearts to players */
    enum class AddHeartResult {
        SUCCESS,        // Heart successfully added
        SAME_HEART,     // Player already has this heart type
        COOLDOWN_ACTIVE // Player must wait before switching hearts
    }

    /** Flag to track if initialization is complete */
    private var done: Boolean = false

    /** JSON serializer for saving/loading heart data */
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    /** Maps player UUIDs to their currently active heart */
    private val playerHearts: MutableMap<UUID, Heart> = mutableMapOf()

    /**
     * Attempts to give a heart to a player.
     *
     * @param playerUUID The UUID of the player to give the heart to
     * @param heart The heart type to give
     * @return AddHeartResult indicating success or reason for failure
     */
    fun add(playerUUID: UUID, heart: Heart): AddHeartResult {
        val currentHeart = playerHearts[playerUUID]

        // Check if player already has this exact heart type
        if (currentHeart != null && currentHeart::class == heart::class) {
            return AddHeartResult.SAME_HEART
        }

        // Check if current heart has an active cooldown (prevents heart swapping abuse)
        currentHeart?.getCooldown(playerUUID)?.let { cooldown ->
            if (cooldown > 0L) {
                return AddHeartResult.COOLDOWN_ACTIVE
            }
        }

        // If player has a different heart, give them back the item before switching
        if (currentHeart != null) {
            val player = Bukkit.getPlayer(playerUUID)
            player?.inventory?.addItem(currentHeart.createItem())
        }

        // Assign the new heart
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

    fun hasCooldown(playerId: UUID): Boolean {
        return allHearts.any { it.getCooldown(playerId) > 0L }
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
