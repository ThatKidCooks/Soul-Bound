package site.thatkid.soulBound.managers.hearts.advancments

import com.google.gson.GsonBuilder
import net.axay.kspigot.event.listen
import net.axay.kspigot.event.register
import net.axay.kspigot.event.unregister
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.plugin.java.JavaPlugin
import site.thatkid.soulBound.items.HeartRegistry
import java.io.File
import java.util.UUID

/**
 * GhastlyListener manages the "Ghastly Heart" achievement system.
 * 
 * The Ghastly Heart is awarded to the first player who completes two specific
 * Nether-related advancements:
 * 1. "Return to Sender" - Kill a ghast with a fireball
 * 2. "Uneasy Alliance" - Rescue a ghast from the Nether, bring it safely home to the Overworld
 * 
 * Key Features:
 * - Tracks completion of specific Nether advancements per player
 * - Awards the Ghastly Heart when a player completes both required advancements
 * - Provides progress feedback showing which advancements are completed
 * - Persists data across server restarts
 * - Ensures only one player can receive the heart
 * 
 * @param plugin The JavaPlugin instance for file operations and logging
 */
class GhastlyListener(private val plugin: JavaPlugin) {

    /**
     * Data class for JSON serialization of the ghastly heart progress.
     * 
     * @property returnDone Map tracking which players have completed "Return to Sender"
     * @property uneasyDone Map tracking which players have completed "Uneasy Alliance"
     * @property received Whether the Ghastly Heart has been awarded to any player
     */
    data class SaveData(
        val returnDone: MutableMap<UUID, Boolean> = mutableMapOf(),
        val uneasyDone: MutableMap<UUID, Boolean> = mutableMapOf(),
        val received: Boolean = false
    )

    /** 
     * Tracks which players have completed the "Return to Sender" advancement.
     * Key: Player UUID, Value: Whether they have completed this advancement
     */
    private var returnDone: MutableMap<UUID, Boolean> = mutableMapOf()
    
    /** 
     * Tracks which players have completed the "Uneasy Alliance" advancement.
     * Key: Player UUID, Value: Whether they have completed this advancement
     */
    private var uneasyDone: MutableMap<UUID, Boolean> = mutableMapOf()

    /** Flag indicating whether the Ghastly Heart has been awarded to any player */
    private var received = false

    /** JSON serializer for save/load operations */
    private val gson = GsonBuilder().setPrettyPrinting().create()
    
    /** File path for persistent data storage */
    private val file = File(plugin.dataFolder, "ghastly.json")

    /**
     * Event listener for player advancement completion events.
     * 
     * This tracks when players complete the specific advancements required for
     * the Ghastly Heart and awards it when both requirements are met by the same player.
     * 
     * Required advancements:
     * - "nether/return_to_sender": Kill a ghast with a fireball
     * - "nether/uneasy_alliance": Rescue a ghast from the Nether
     */
    val listener = listen<PlayerAdvancementDoneEvent> {
        val player = it.player
        val playerId = player.uniqueId
        val advancement = it.advancement.key.key

        // Track completion of the required advancements
        if (advancement == "nether/return_to_sender") returnDone[playerId] = true
        if (advancement == "nether/uneasy_alliance") uneasyDone[playerId] = true

        // Check if this player has now completed both required advancements
        if (returnDone[playerId] == true && uneasyDone[playerId] == true) {
            if (!received) {
                // Award the Ghastly Heart to this player
                val ghastlyHeart = HeartRegistry.hearts["ghastly"]?.createItem()
                if (ghastlyHeart != null) {
                    player.inventory.addItem(ghastlyHeart)
                    player.server.broadcast(Component.text("§aThe Ghastly Heart has been awarded to ${player.name} for completing the required advancements!"))
                    received = true // Prevent future awards
                    save() // Persist the state immediately
                }
            } else {
                // Heart already awarded to someone else
                player.sendMessage("§cA player has already received the Ghastly Heart.")
            }
        }
    }

    /**
     * Enables this listener and loads any existing data from disk.
     * Should be called when the plugin starts or when this heart system is activated.
     */
    fun enable() {
        listener.register()
        load() // Load existing progress from disk
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
     * Saves the current ghastly heart progress to a JSON file.
     */
    fun save() {
        try {
            val saveData = SaveData(returnDone, uneasyDone, received)
            val json = gson.toJson(saveData)
            file.parentFile?.mkdirs() // Ensure directory exists
            file.writeText(json)
            plugin.logger.info("Ghastly data saved to ${file.absolutePath}")
        } catch (ex: Exception) {
            plugin.logger.warning("Failed to save ghastly.json: ${ex.message}")
        }
    }

    /**
     * Loads ghastly heart progress from the JSON file.
     * If the file doesn't exist, starts with empty data.
     */
    fun load() {
        try {
            if (!file.exists()) return
            
            val json = file.readText()
            val saveData = gson.fromJson(json, SaveData::class.java)
            returnDone = saveData.returnDone
            uneasyDone = saveData.uneasyDone
            received = saveData.received
            
            plugin.logger.info("Ghastly data loaded from ${file.absolutePath}")
        } catch (ex: Exception) {
            plugin.logger.warning("Failed to load ghastly.json: ${ex.message}")
            returnDone = mutableMapOf()
            uneasyDone = mutableMapOf()
            received = false
        }
    }

    /**
     * Gets a formatted progress string for the specified player.
     * Shows which of the two required advancements have been completed.
     * 
     * @param playerId The UUID of the player to check
     * @return A formatted string showing advancement completion status
     */
    fun getProgress(playerId: UUID): String {
        val playerName = Bukkit.getPlayer(playerId)?.name ?: "Unknown Player"
        val msg = "§a$playerName has gotten the following advancements for the Ghastly Heart:\n" +
                "§aReturn to Sender: ${if (returnDone[playerId] == true) "§aDone" else "§cNot Done"}\n" +
                "§aUneasy Alliance: ${if (uneasyDone[playerId] == true) "§aDone" else "§cNot Done"}"

        if (received) {
            return "$msg\n§cThe Ghastly Heart has already been received by a player."
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