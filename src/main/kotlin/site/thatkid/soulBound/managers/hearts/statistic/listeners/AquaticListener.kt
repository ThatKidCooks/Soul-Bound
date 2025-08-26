package site.thatkid.soulBound.managers.hearts.statistic.listeners

import com.google.gson.GsonBuilder
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import site.thatkid.soulBound.items.HeartRegistry
import site.thatkid.soulBound.managers.hearts.statistic.Statistic
import java.io.File

/**
 * AquaticListener manages the "Aquatic Heart" achievement system.
 * 
 * The Aquatic Heart is awarded to the first player who swims 5,000 blocks (500,000 cm).
 * This is a statistic-based heart that uses Minecraft's built-in SWIM_ONE_CM statistic
 * to track player swimming distance across all sessions.
 * 
 * Key Features:
 * - Uses Bukkit's persistent statistic system for accurate tracking
 * - Periodically checked by the Caller system (not event-based)
 * - Awards the Aquatic Heart to the first player reaching the swimming milestone
 * - Provides progress feedback through swimming distance queries
 * - Persists award status across server restarts
 * - Ensures only one player can receive the heart
 * 
 * Note: This is a statistic-based heart, meaning it's checked periodically by the
 * Caller system rather than responding to specific events.
 * 
 * @see site.thatkid.soulBound.managers.hearts.statistic.Caller
 * @see site.thatkid.soulBound.managers.hearts.statistic.Statistic
 */
class AquaticListener {

    /**
     * Data class for JSON serialization of the aquatic heart status.
     * Since this is statistic-based, we only need to track if it was awarded.
     * 
     * @property received Whether the Aquatic Heart has been awarded to any player
     */
    data class SaveData(
        val received: Boolean = false
    )

    /** Flag indicating whether the Aquatic Heart has been awarded to any player */
    var received = false

    /** JSON serializer for save/load operations */
    private val gson = GsonBuilder().setPrettyPrinting().create()
    
    /** File path for persistent data storage */
    private val file = File(plugin.dataFolder, "aquatic.json")

    /** 
     * Gets the plugin instance using the providing plugin mechanism.
     * This allows the listener to access plugin resources without a direct reference.
     */
    val plugin: JavaPlugin
        get() = JavaPlugin.getProvidingPlugin(AquaticListener::class.java)

    /**
     * Checks all online players for the swimming milestone and awards the Aquatic Heart.
     * 
     * This method is called periodically by the Caller system. It examines each online
     * player's SWIM_ONE_CM statistic and awards the Aquatic Heart to the first player
     * who has swum 5,000 blocks (500,000 cm).
     * 
     * @param statistic The Statistic helper for querying player statistics
     */
    fun check(statistic: Statistic) {
        // Check all currently online players
        for (player in plugin.server.onlinePlayers) {
            // Get the swimming distance in centimeters
            val stat = statistic.getStatistic(player, org.bukkit.Statistic.SWIM_ONE_CM)

            // Check if player has swum 5,000 blocks (500,000 cm)
            if (stat >= (5000 * 100)) {
                if (!received) {
                    // Award the Aquatic Heart to this player
                    val aquaticHeart = HeartRegistry.hearts["aquatic"]?.createItem()
                    if (aquaticHeart == null) return

                    player.inventory.addItem(aquaticHeart)
                    plugin.server.broadcast(Component.text("§c${player.name} was the First Person to swim 5000 blocks and has obtained the Aquatic Heart"))
                    received = true // Prevent future awards
                    save() // Persist the state immediately
                }
            }
        }
    }

    /**
     * Saves the current aquatic heart status to a JSON file.
     */
    fun save() {
        try {
            val saveData = SaveData(received)
            val json = gson.toJson(saveData)
            file.parentFile?.mkdirs() // Ensure directory exists
            file.writeText(json)
            plugin.logger.info("Aquatic data saved to ${file.absolutePath}")
        } catch (ex: Exception) {
            plugin.logger.warning("Failed to save aquatic.json: ${ex.message}")
        }
    }

    /**
     * Loads aquatic heart status from the JSON file.
     * If the file doesn't exist, starts with default values.
     */
    fun load() {
        if (!file.exists()) return
        try {
            val json = file.readText()
            val saveData = gson.fromJson(json, SaveData::class.java)
            received = saveData.received
            plugin.logger.info("Aquatic data loaded from ${file.absolutePath}")
        } catch (ex: Exception) {
            plugin.logger.warning("Failed to load aquatic.json: ${ex.message}")
            received = false
        }
    }

    /**
     * Gets the swimming progress for a specific player in blocks.
     * 
     * @param player The player to check swimming progress for
     * @return The number of blocks the player has swum (converted from centimeters)
     */
    fun getProgress(player: Player): Int {
        val stat = player.getStatistic(org.bukkit.Statistic.SWIM_ONE_CM)
        return (stat / 100) // Convert centimeters to blocks
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