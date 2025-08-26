package site.thatkid.soulBound.managers.hearts.mine

import com.google.gson.GsonBuilder
import net.axay.kspigot.event.listen
import net.axay.kspigot.event.register
import net.axay.kspigot.event.unregister
import net.axay.kspigot.extensions.broadcast
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.plugin.java.JavaPlugin
import site.thatkid.soulBound.items.HeartRegistry
import java.io.File
import java.util.UUID

/**
 * HasteListener manages the "Haste Heart" achievement system.
 * 
 * The Haste Heart is awarded to the first player who mines 10,000 deepslate blocks.
 * This includes all variants of deepslate blocks and ores found in the deepslate layer.
 * 
 * Key Features:
 * - Tracks deepslate block mining for all players
 * - Awards the Haste Heart to the first player reaching the goal
 * - Provides progress feedback to players
 * - Persists data across server restarts
 * - Ensures only one player can receive the heart
 * 
 * @param plugin The JavaPlugin instance for file operations and logging
 */
class HasteListener(private val plugin: JavaPlugin) {

    /** File path for persistent data storage */
    private val file = File(plugin.dataFolder, "haste.json")

    /** 
     * Tracks the number of deepslate blocks mined by each player.
     * Key: Player UUID, Value: Number of deepslate blocks mined
     */
    var blocksMined: MutableMap<UUID, Int> = mutableMapOf()
    
    /** Flag indicating whether the Haste Heart has been awarded to any player */
    private var received: Boolean = false

    /** JSON serializer for save/load operations */
    val gson = GsonBuilder().setPrettyPrinting().create()

    /**
     * Data class for JSON serialization of the haste heart progress.
     * 
     * @property blocksMined Map of player UUIDs to their deepslate block mining counts
     * @property received Whether the Haste Heart has been awarded to any player
     */
    data class SaveData(
        val blocksMined: MutableMap<UUID, Int> = mutableMapOf(),
        val received: Boolean = false
    )

    /**
     * Set of all deepslate block types that count toward the Haste Heart.
     * Includes both regular deepslate blocks and all deepslate ore variants.
     */
    private val deepslateTypes = setOf(
        Material.DEEPSLATE,
        Material.COBBLED_DEEPSLATE,
        Material.DEEPSLATE_COAL_ORE,
        Material.DEEPSLATE_IRON_ORE,
        Material.DEEPSLATE_COPPER_ORE,
        Material.DEEPSLATE_GOLD_ORE,
        Material.DEEPSLATE_REDSTONE_ORE,
        Material.DEEPSLATE_LAPIS_ORE,
        Material.DEEPSLATE_DIAMOND_ORE,
        Material.DEEPSLATE_EMERALD_ORE
    )

    /**
     * Event listener for block break events.
     * 
     * This tracks deepslate block mining and awards the Haste Heart when a player
     * reaches the 10,000 block milestone. Provides feedback to players about their
     * progress and the status of the heart.
     */
    private val listener = listen<BlockBreakEvent> {
        val player = it.player
        val playerId = player.uniqueId
        val blocks = blocksMined.computeIfAbsent(playerId) { 0 }
        
        // Only count deepslate blocks toward the achievement
        if (!deepslateTypes.contains(it.block.type)) return@listen
        
        // Increment the block count for this player
        blocksMined[playerId] = blocks + 1

        // Check if the player has reached the milestone
        if (blocksMined[playerId]!! >= 10000) {
            if (!received) {
                // Award the Haste Heart to this player
                val hasteHeart = HeartRegistry.hearts["haste"]?.createItem()
                if (hasteHeart != null) {
                    player.inventory.addItem(hasteHeart)
                    broadcast("The Haste Heart has been awarded to ${player.name} for mining 10,000 Deepslate Blocks First!")
                    received = true // Prevent future awards
                    save() // Persist the state immediately
                }
            } else {
                // Heart already awarded to someone else
                player.sendMessage("§7Someone already received the Haste Heart.")
            }
        } else {
            // Show progress to the player
            val remaining = 10000 - blocksMined[playerId]!!
            player.sendMessage("§7You need $remaining more blocks to receive the Haste Heart.")
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
     * Loads haste heart progress from the JSON file.
     * If the file doesn't exist, starts with empty data.
     */
    fun load() {
        if (!file.exists()) return
        try {
            val json = file.readText()
            val saveData = gson.fromJson(json, SaveData::class.java)
            blocksMined = saveData.blocksMined.toMutableMap()
            received = saveData.received
            plugin.logger.info("Haste data loaded from ${file.absolutePath}")
        } catch (ex: Exception) {
            plugin.logger.warning("Failed to load haste.json: ${ex.message}")
            blocksMined = mutableMapOf()
            received = false
        }
    }

    /**
     * Saves the current haste heart progress to a JSON file.
     */
    fun save() {
        try {
            val saveData = SaveData(blocksMined, received)
            val json = gson.toJson(saveData)
            file.parentFile?.mkdirs() // Ensure directory exists
            file.writeText(json)
            plugin.logger.info("Haste data saved to ${file.absolutePath}")
        } catch (ex: Exception) {
            plugin.logger.warning("Failed to save haste.json: ${ex.message}")
        }
    }

    /**
     * Gets a formatted progress string for the specified player.
     * 
     * @param playerId The UUID of the player to check
     * @return A formatted string showing mining progress and completion percentage
     */
    fun getProgress(playerId: UUID): String {
        val blocks = blocksMined.computeIfAbsent(playerId) { 0 }
        val total = 10000
        val percent = ((blocks * 100) / total).coerceAtMost(100)

        val msg = "§${Bukkit.getPlayer(playerId)} has mined §e$blocks §blocks out of $total. §f($percent%)"

        if (received) {
            return "$msg §cThe Haste heart has already been received by a player."
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