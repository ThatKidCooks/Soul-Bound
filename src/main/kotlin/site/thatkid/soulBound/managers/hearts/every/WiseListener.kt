package site.thatkid.soulBound.managers.hearts.every

import com.google.gson.GsonBuilder
import net.axay.kspigot.event.listen
import net.axay.kspigot.event.register
import net.axay.kspigot.event.unregister
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.BrewingStand
import org.bukkit.entity.Player
import org.bukkit.event.inventory.BrewEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffectType
import site.thatkid.soulBound.items.HeartRegistry
import java.io.File
import java.io.IOException
import java.util.*

/**
 * WiseListener manages the "Wise Heart" achievement system.
 * 
 * The Wise Heart is a rare heart awarded to the first player who brews all unique potion effects.
 * This is one of the most complex heart systems as it must track potion brewing across multiple
 * events and handle the intricate relationship between brewing stands, players, and potion types.
 * 
 * Key Features:
 * - Tracks inventory clicks to associate players with brewing stands
 * - Monitors brew events to detect when potions are successfully created
 * - Maintains a registry of all unique potion effects each player has brewed
 * - Awards the Wise Heart when a player has brewed all possible potion effects
 * - Handles serialization complexities with PotionEffectType conversion
 * - Persists data across server restarts
 * - Ensures only one player can receive the heart
 * 
 * Complex Event Handling:
 * Since brewing is a multi-step process, this listener must:
 * 1. Track which player last interacted with each brewing stand (InventoryClickEvent)
 * 2. When brewing completes (BrewEvent), associate the brewed potions with that player
 * 3. Extract potion effect types from the resulting items and add to player's collection
 * 4. Check if the player has now brewed all possible effects and award the heart
 * 
 * Serialization Logic:
 * PotionEffectType cannot be directly serialized to JSON, so this system:
 * - Converts PotionEffectType to String keys for storage
 * - Converts String keys back to PotionEffectType on load
 * - Handles cases where effect types may have changed between server versions
 * 
 * @param plugin The JavaPlugin instance for file operations and logging
 */
class WiseListener(private val plugin: JavaPlugin) {

    /**
     * Data class for JSON serialization of the wise heart progress.
     * 
     * @property brewedPotions Map of player UUIDs to sets of potion effect type names (as strings)
     * @property received Whether the Wise Heart has been awarded to any player
     */
    data class SaveData(
        val brewedPotions: MutableMap<UUID, MutableSet<String>> = mutableMapOf(),
        val received: Boolean = false
    )

    /** JSON serializer for save/load operations */
    private val gson = GsonBuilder().setPrettyPrinting().create()
    
    /** File path for persistent data storage */
    private val file = File(plugin.dataFolder, "wise.json")

    /** 
     * Tracks all unique potion effects each player has successfully brewed.
     * Key: Player UUID, Value: Set of PotionEffectTypes they have created
     */
    private var brewedPotions: MutableMap<UUID, MutableSet<PotionEffectType>> = mutableMapOf()

    /** Flag indicating whether the Wise Heart has been awarded to any player */
    private var received = false

    /** 
     * Maps brewing stand locations to the UUID of the last player who interacted with them.
     * This is essential because BrewEvent doesn't directly tell us which player initiated the brewing.
     */
    private var lastBrewer: MutableMap<Location, UUID> = mutableMapOf()

    /** Transient reference for serialization compatibility */
    @Transient
    private var someReference: java.lang.ref.Reference<*>? = null

    /**
     * Event listener for inventory clicks on brewing stands.
     * 
     * This captures when players interact with brewing stand inventories, allowing us to
     * associate the player with that brewing stand location. When a brew event occurs,
     * we can then determine which player initiated the brewing process.
     */
    val inventoryClick = listen<InventoryClickEvent> { event ->
        val stand = event.inventory.holder as? BrewingStand ?: return@listen
        val player = event.whoClicked as? Player ?: return@listen
        
        // Record this player as the last one to interact with this brewing stand
        lastBrewer[stand.location] = player.uniqueId
    }

    /**
     * Event listener for completed brewing events.
     * 
     * This is triggered when a brewing stand finishes creating potions. It:
     * 1. Identifies which player initiated the brewing (from inventory click tracking)
     * 2. Examines all resulting potions for their effect types
     * 3. Adds any new effect types to the player's collection
     * 4. Checks if the player has now brewed all possible effects and awards the heart
    val brewEvent = listen<BrewEvent> { event ->
        // Get the player who last interacted with this brewing stand
        val brewerId = lastBrewer[event.block.location] ?: return@listen
        val brewer = Bukkit.getPlayer(brewerId) ?: return@listen

        // Examine all items produced by this brewing event
        for (item in event.contents) {
            if (item == null) continue
            val meta = item.itemMeta
            if (meta is PotionMeta) {
                val effect = meta.basePotionType?.effectType ?: continue

                println("Potion brewed by ${brewer.name}: $effect") // Debug output

                // Add this effect type to the player's collection of brewed effects
                val set = brewedPotions.computeIfAbsent(brewer.uniqueId) { mutableSetOf() }
                set.add(effect)
            }
        }

        // Check if this player has now brewed all possible potion effects
        val craftablePotionEffects = PotionEffectType.values()
            .filterNotNull().toSet()

        if (!received && brewedPotions.values.any { it.containsAll(craftablePotionEffects) }) {
            // Award the Wise Heart - first player to brew all effects wins
            plugin.server.broadcast(Component.text("§a${brewer.name} has brewed all possible potion effects and received the Wise Heart!"))
            val wiseHeart = HeartRegistry.hearts["wise"]?.createItem()

            if (wiseHeart != null) {
                brewer.inventory.addItem(wiseHeart)
            }
            received = true // Prevent future awards
            save() // Persist the achievement immediately
        }
    }

    /**
     * Enables this listener and loads any existing data from disk.
     * Should be called when the plugin starts or when this heart system is activated.
     */
    fun enable() {
        inventoryClick.register()
        brewEvent.register()
        load() // Load existing progress from disk
    }

    /**
     * Disables this listener and saves current data to disk.
     * Should be called when the plugin stops or when this heart system is deactivated.
     */
    fun disable() {
        inventoryClick.unregister()
        brewEvent.unregister()
        save() // Persist current progress before shutdown
    }

    /**
     * Saves the current wise heart progress to a JSON file.
     * 
     * This method handles the complex serialization required for PotionEffectType objects:
     * 1. Converts PotionEffectType objects to their string names for JSON compatibility
     * 2. Creates a SaveData object with the serialized data and received status
     * 3. Writes the JSON to disk with proper error handling
     */
    fun save() {
        try {
            // Convert PotionEffectType objects to String names for JSON serialization
            val serializedPotions = brewedPotions.mapValues { (_, effects) ->
                effects.mapNotNull { it.name }.toMutableSet()
            }
            val saveData = SaveData(serializedPotions as MutableMap<UUID, MutableSet<String>>, received)
            val json = gson.toJson(saveData)
            file.parentFile.mkdirs() // Ensure directory exists
            file.writeText(json)
            plugin.logger.info("WiseListener data saved to ${file.absolutePath}")
        } catch (e: Exception) {
            plugin.logger.severe("Failed to save WiseListener data: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Loads wise heart progress from the JSON file.
     * 
     * This method handles the complex deserialization required for PotionEffectType objects:
     * 1. Reads and parses the JSON data from disk
     * 2. Converts string names back to PotionEffectType objects
     * 3. Handles cases where effect types may no longer exist (version changes)
     * 4. Restores the received status and all player progress
     */
    fun load() {
        try {
            if (!file.exists()) return
            val json = file.readText()
            val saveData = gson.fromJson(json, SaveData::class.java)
            
            // Convert String names back to PotionEffectType objects, filtering out any that no longer exist
            brewedPotions = saveData.brewedPotions.mapValues { (_, effectNames) ->
                effectNames.mapNotNull { PotionEffectType.getByName(it) }.toMutableSet()
            }.toMutableMap()
            
            received = saveData.received
            plugin.logger.info("WiseListener data loaded from ${file.absolutePath}")
        } catch (e: IOException) {
            plugin.logger.severe("Failed to load WiseListener data: ${e.message}")
            e.printStackTrace()
        }
    }
}