package site.thatkid.soulBound.managers.hearts.kill

import com.google.gson.GsonBuilder
import net.axay.kspigot.event.listen
import net.axay.kspigot.event.register
import net.axay.kspigot.event.unregister
import net.axay.kspigot.extensions.broadcast
import org.bukkit.Bukkit
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.plugin.java.JavaPlugin
import site.thatkid.soulBound.items.HeartRegistry
import java.io.File
import java.util.UUID

/**
 * CrownedListener manages the "Crowned Heart" achievement system.
 * 
 * The Crowned Heart is a rare heart awarded to the first player who kills 15 unique players in PvP.
 * This heart has a special dependency - it can only be obtained AFTER the Strength Heart has been
 * awarded to someone, creating a progression system where PvE achievements unlock PvP achievements.
 * 
 * Key Features:
 * - Tracks unique PvP kills per player (duplicate kills of same player don't count)
 * - Requires Strength Heart to be obtained first (linked progression system)
 * - Awards the Crowned Heart to the first player reaching 15 unique kills
 * - Provides progress feedback to players
 * - Persists data across server restarts
 * - Ensures only one player can receive the heart
 * 
 * Linked System:
 * The Crowned and Strength hearts are interconnected - the StrengthListener must award
 * its heart before any PvP kills will count toward the Crowned Heart.
 * 
 * @param plugin The JavaPlugin instance for file operations and logging
 */
class CrownedListener(private val plugin: JavaPlugin) {

    /**
     * Data class for JSON serialization of the crowned heart progress.
     * 
     * @property kills Map of killer UUIDs to lists of victim UUIDs they have killed
     * @property received Whether the Crowned Heart has been awarded to any player
     */
    private data class SaveData (
        val kills: MutableMap<UUID, MutableList<UUID>> = mutableMapOf(),
        val received: Boolean = false
    )

    /** 
     * Reference to the StrengthListener for checking the linked progression system.
     * The Crowned Heart can only be obtained after the Strength Heart has been awarded.
     */
    lateinit var strengthListener: StrengthListener

    /** File path for persistent data storage */
    private val file = File(plugin.dataFolder, "crowned.json")

    /** 
     * Tracks PvP kills by each player.
     * Key: Killer UUID, Value: List of unique victim UUIDs they have killed
     * Using a list of UUIDs ensures each victim is only counted once per killer.
     */
    var kills: MutableMap<UUID, MutableList<UUID>> = mutableMapOf()
    
    /** Flag indicating whether the Crowned Heart has been awarded to any player */
    private var received: Boolean = false

    /** JSON serializer for save/load operations */
    val gson = GsonBuilder().setPrettyPrinting().create()

    /**
     * Event listener for player death events.
     * 
     * This tracks PvP kills and awards the Crowned Heart when a player reaches 15 unique kills.
     * Implements the linked progression system where the Strength Heart must be obtained first.
     * Only counts unique kills - killing the same player multiple times doesn't increase the count.
     */
    private val listener = listen<PlayerDeathEvent> {
        val victim = it.entity
        val victimId = victim.uniqueId
        val killer = it.entity.killer ?: return@listen
        val killerId = killer.uniqueId
        
        // Get or create the kill list for this killer
        val victims = kills.computeIfAbsent(killerId) { mutableListOf() }
        
        // Check if Strength Heart has been obtained (linked progression requirement)
        if (!strengthListener.received) {
            killer.sendMessage("You killed ${victim.name}, however this kill will go to the strength heart progress as that hasn't been earned yet.")
            return@listen
        }
        
        // Only count unique victims (no duplicate kills)
        if (!victims.contains(victimId)) {
            victims.add(victimId)
        }

        // Check if this killer has reached the milestone
        if (victims.size >= 15) {
            if (!received) {
                // Award the Crowned Heart to this player
                val crownedHeart = HeartRegistry.hearts["crowned"]?.createItem()
                if (crownedHeart != null) {
                    killer.inventory.addItem(crownedHeart)
                    broadcast("The Crowned Heart has been awarded to ${killer.name} for killing 15 Players First!")
                    received = true // Prevent future awards
                    save() // Persist the state immediately
                }
            } else {
                // Heart already awarded to someone else
                killer.sendMessage("§7Someone already received the Crowned Heart.")
            }
        } else {
            // Show progress to the killer (note: there's a bug in the original - should be 15, not 5)
            val remaining = 15 - victims.size
            killer.sendMessage("§7You need $remaining more kills to receive the Crowned Heart.")
        }
    }

    /**
     * Enables this listener and loads any existing data from disk.
     * Should be called when the plugin starts or when this heart system is activated.
     */
    fun enable() {
        load() // Load existing progress from disk
        listener.register()
    }

    /**
     * Disables this listener and saves current data to disk.
     * Should be called when the plugin stops or when this heart system is deactivated.
     */
    fun disable() {
        listener.unregister()
        save() // Persist current progress before shutdown
    }

    /**
     * Loads crowned heart progress from the JSON file.
     * If the file doesn't exist, starts with empty data.
     */
    fun load() {
        if (!file.exists()) return
        try {
            val json = file.readText()
            val saveData = gson.fromJson(json, SaveData::class.java)
            kills = saveData.kills.toMutableMap()
            received = saveData.received
            plugin.logger.info("Crowned data loaded from ${file.absolutePath}")
        } catch (ex: Exception) {
            plugin.logger.warning("Failed to load crowned.json: ${ex.message}")
            kills = mutableMapOf()
            received = false
        }
    }

    /**
     * Saves the current crowned heart progress to a JSON file.
     */
    fun save() {
        try {
            val saveData = SaveData(kills, received)
            val json = gson.toJson(saveData)
            file.parentFile?.mkdirs() // Ensure directory exists
            file.writeText(json)
            plugin.logger.info("Crowned data saved to ${file.absolutePath}")
        } catch (ex: Exception) {
            plugin.logger.warning("Failed to save crowned.json: ${ex.message}")
        }
    }

    /**
     * Gets a formatted progress string for the specified player.
     * Shows how many unique players they have killed and progress toward the goal.
     * 
     * @param playerId The UUID of the player to check
     * @return A formatted string showing kill progress and completion percentage
     */
    fun getProgress(playerId: UUID): String {
        val victims = kills.computeIfAbsent(playerId) { mutableListOf() }
        val total = 15
        val killCount = victims.size
        val percent = ((killCount * 100) / total).coerceAtMost(100)

        val playerName = Bukkit.getPlayer(playerId)?.name ?: "Unknown Player"
        val msg = "§a$playerName has killed §e$killCount §7unique players out of $total. §f($percent%)"

        if (received) {
            return "$msg §cThe Crowned heart has already been received by a player."
        }
        return msg
    }

    /**
     * Manually sets the global received status.
     * Used for administrative purposes or testing.
     * 
     * @param received Whether the heart should be marked as received
     */
    fun setGlobalReceived(received: Boolean) {
        this.received = received
    }
}