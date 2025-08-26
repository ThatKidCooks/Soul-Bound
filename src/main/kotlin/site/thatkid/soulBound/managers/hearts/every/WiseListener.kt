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
 * The Wise Heart is awarded to the first player who successfully brews all possible potion effects.
 * This listener tracks player brewing activities and maintains a record of which potion effects
 * each player has successfully brewed.
 * 
 * Key Features:
 * - Tracks brewing stand interactions to identify the brewer
 * - Records all unique potion effects brewed by each player
 * - Awards the Wise Heart when a player has brewed all possible effects
 * - Persists data to JSON file for server restarts
 * - Ensures only one player can receive the heart (first-come-first-served)
 * 
 * @param plugin The JavaPlugin instance for file operations and logging
 */
class WiseListener(private val plugin: JavaPlugin) {

    /**
     * Data class for JSON serialization of the wise heart progress.
     * Stores potion effects as strings to avoid serialization issues with PotionEffectType.
     * 
     * @property brewedPotions Map of player UUIDs to sets of potion effect names they have brewed
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
     * Runtime tracking of brewed potions by player.
     * Key: Player UUID, Value: Set of PotionEffectType objects they have brewed
     */
    private var brewedPotions: MutableMap<UUID, MutableSet<PotionEffectType>> = mutableMapOf()

    /** Flag indicating whether the Wise Heart has been awarded to any player */
    private var received = false

    /** 
     * Tracks the last player to interact with each brewing stand.
     * Key: BrewingStand Location, Value: Player UUID who last clicked it
     * This is necessary because BrewEvent doesn't directly tell us who initiated the brewing.
     */
    private var lastBrewer: MutableMap<Location, UUID> = mutableMapOf()

    /** Transient field for potential future use - not persisted */
    @Transient
    private var someReference: java.lang.ref.Reference<*>? = null

    /**
     * Event listener for inventory clicks on brewing stands.
     * 
     * This tracks which player last interacted with each brewing stand so we can
     * attribute the resulting brewed potions to the correct player when the 
     * BrewEvent fires. The BrewEvent itself doesn't contain player information.
     * 
     * @see BrewEvent
     */
    val inventoryClick = listen<InventoryClickEvent> { event ->
        // Only process clicks on brewing stand inventories
        val stand = event.inventory.holder as? BrewingStand ?: return@listen
        val player = event.whoClicked as? Player ?: return@listen
        
        // Record this player as the last one to interact with this brewing stand
        lastBrewer[stand.location] = player.uniqueId
    }

    /**
     * Event listener for completed brewing events.
     * 
     * This is the main logic for tracking brewed potions and awarding the Wise Heart.
     * When a brewing operation completes, this examines all resulting potions and
     * records their effect types for the player who initiated the brewing.
     * 
     * Awards the Wise Heart if the player has now brewed all possible potion effects.
     */
    val brewEvent = listen<BrewEvent> { event ->
        // Find which player initiated this brewing operation
        val brewerId = lastBrewer[event.block.location] ?: return@listen
        val brewer = Bukkit.getPlayer(brewerId) ?: return@listen

        // Examine all items produced by the brewing operation
        for (item in event.contents) {
            if (item == null) continue
            val meta = item.itemMeta
            
            // Only process potion items
            if (meta is PotionMeta) {
                // Extract the potion effect type from the meta
                val effect = meta.basePotionType?.effectType ?: continue

                println("Potion brewed by ${brewer.name}: $effect") // Debug logging

                // Add this effect to the player's brewed effects set
                val set = brewedPotions.computeIfAbsent(brewer.uniqueId) { mutableSetOf() }
                set.add(effect)
            }
        }

        // Check if this player has now brewed all possible potion effects
        val craftablePotionEffects = PotionEffectType.values()
            .filterNotNull().toSet()

        // Award the Wise Heart if:
        // 1. No one has received it yet
        // 2. Any player has brewed all possible effects
        if (!received && brewedPotions.values.any { it.containsAll(craftablePotionEffects) }) {
            // Announce the achievement to the server
            plugin.server.broadcast(Component.text("§a${brewer.name} has brewed all possible potion effects and received the Wise Heart!"))
            
            // Create and give the Wise Heart item
            val wiseHeart = HeartRegistry.hearts["wise"]?.createItem()
            if (wiseHeart != null) {
                brewer.inventory.addItem(wiseHeart)
            }
            
            // Mark as received to prevent future awards
            received = true
            save() // Persist the state immediately
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
     * Converts PotionEffectType objects to strings for JSON serialization,
     * as the enum types cannot be directly serialized.
     */
    fun save() {
        try {
            // Convert PotionEffectType objects to string names for serialization
            val serializedPotions = brewedPotions.mapValues { (_, effects) ->
                effects.mapNotNull { it.name }.toMutableSet()
            }
            
            val saveData = SaveData(serializedPotions as MutableMap<UUID, MutableSet<String>>, received)
            val json = gson.toJson(saveData)
            
            // Ensure directory exists and write the file
            file.parentFile.mkdirs()
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
     * Converts string potion effect names back to PotionEffectType objects
     * for runtime use. If the file doesn't exist, starts with empty data.
     */
    fun load() {
        try {
            if (!file.exists()) return
            
            val json = file.readText()
            val saveData = gson.fromJson(json, SaveData::class.java)
            
            // Convert string effect names back to PotionEffectType objects
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