package site.thatkid.soulBound.managers

import com.google.gson.GsonBuilder
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.plugin.java.JavaPlugin
import site.thatkid.soulBound.items.hearts.Trader
import java.io.File
import java.util.*

/**
 * Tracks progress toward obtaining the Trader Heart.
 * 
 * **Requirements for Trader Heart:**
 * - Trade with all 13 different villager professions
 * - Must be the first player to complete this requirement
 * 
 * **Villager Professions Required:**
 * Armorer, Butcher, Cartographer, Cleric, Farmer, Fisherman, Fletcher,
 * Leatherworker, Librarian, Mason, Shepherd, Toolsmith, Weaponsmith
 * 
 * This tracker monitors villager trading GUI interactions and tracks
 * which professions each player has traded with. When a player completes
 * all 13 professions and no one else has the heart yet, they are awarded
 * the Trader Heart.
 * 
 * **Data Persistence:**
 * - Player progress saved to trader_heart.json
 * - Tracks both individual progress and the global winner
 * 
 * @param plugin Reference to the main plugin instance for file management
 */
class TraderHeartTracker(private val plugin: JavaPlugin) : Listener {

    /** JSON serializer for saving/loading data */
    private val gson = GsonBuilder().setPrettyPrinting().create()
    
    /** File where trader progress is saved */
    private val dataFile = File(plugin.dataFolder, "trader_heart.json")

    /** Tracks which professions each player has traded with */
    private val professionTrades: MutableMap<UUID, MutableSet<Villager.Profession>> = mutableMapOf()
    
    /** The single player who has received the heart (null if no one has it yet) */
    private var heartWinner: UUID? = null

    /**
     * Initializes the tracker and starts monitoring trading events.
     * 
     * Should be called from the plugin's onEnable() method.
     */
    fun enable() {
        // Ensure plugin folder exists
        plugin.dataFolder.mkdirs()
        
        // Load any existing saved data
        load()
        
        // Register this class as an event listener
        plugin.server.pluginManager.registerEvents(this, plugin)
        plugin.logger.info("[TraderHeartTracker] Enabled – listening for villager trade clicks")
    }

    /**
     * Saves all data and performs cleanup.
     * 
     * Should be called from the plugin's onDisable() method.
     */
     */
    fun disable() {
        save()
        plugin.logger.info("[TraderHeartTracker] Disabled – saved ${professionTrades.size} players' progress")
    }

    /**
     * Listens for clicks in a MERCHANT (villager) GUI. Slot 2 is the result slot.
     */
    @EventHandler
    fun onTradeClick(e: InventoryClickEvent) {
        // if someone already has the heart, no one else can earn it
        if (heartWinner != null) return

        // must be trading with a MERCHANT (villager) GUI
        if (e.view.topInventory.type != InventoryType.MERCHANT) return
        // result slot index is 2
        if (e.slot != 2) return
        // no result item = no completed trade
        if (e.currentItem == null) return
        // only players
        val player = e.whoClicked as? Player ?: return

        // the inventory holder is the Villager
        val holder = e.view.topInventory.holder
        val villager = holder as? Villager ?: return
        val profession = villager.profession

        // skip "no-trade" villagers
        if (profession == Villager.Profession.NONE || profession == Villager.Profession.NITWIT) return

        val uuid = player.uniqueId
        val set = professionTrades.getOrPut(uuid) { mutableSetOf() }
        val newlyAdded = set.add(profession)
        if (!newlyAdded) {
            // already traded with this profession
            player.sendMessage("§eYou've already traded with a ${profession.name().lowercase().replace('_', ' ')}.")
            return
        }

        // compute progress
        val progress  = set.size
        val total     = Villager.Profession.values().count { it != Villager.Profession.NONE && it != Villager.Profession.NITWIT }
        val remaining = total - progress

        // send feedback
        player.sendMessage("§aTraded with a ${profession.name().lowercase().replace('_',' ').replaceFirstChar(Char::uppercase)}.")
        player.sendMessage("§aProgress: $progress/$total professions complete. §7($remaining more to go)")

        // grant heart if complete (and no one else has claimed it yet)
        if (progress >= total && heartWinner == null) {
            player.inventory.addItem(Trader.createItem())
            player.sendMessage("§2You are the first to trade with all professions and have earned the §lTrader Heart§r§2!")
            Bukkit.broadcastMessage("§a${player.name} is the first to earn the §lTrader Heart§r§a!")
            heartWinner = uuid
        }

        // immediately persist if you like
        save()
    }

    fun getProgress(uuid: UUID): Int = professionTrades[uuid]?.size ?: 0

    fun getTotalRequired(): Int =
        Villager.Profession.values().count { it != Villager.Profession.NONE && it != Villager.Profession.NITWIT }

    fun hasReceived(uuid: UUID): Boolean = heartWinner == uuid


    /**
     * Save progress to JSON
     */
     fun save() {
        val content = mutableMapOf<String, Any>()
        professionTrades.forEach { (uuid, profs) ->
            content[uuid.toString()] = profs.map(Villager.Profession::toString)
        }
        // save the winner's UUID
        heartWinner?.let { content["__winner__"] = it.toString() }
        dataFile.writeText(gson.toJson(content))
    }

    /**
     * Load progress from JSON
     */
    private fun load() {
        if (!dataFile.exists()) return
        try {
            @Suppress("UNCHECKED_CAST")
            val tree = gson.fromJson(dataFile.readText(), Map::class.java) as Map<String, Any>
            tree.forEach { (key, value) ->
                if (key == "__winner__") {
                    heartWinner = UUID.fromString(value as String)
                } else {
                    val uuid = UUID.fromString(key)
                    @Suppress("UNCHECKED_CAST")
                    val profList = (value as? List<*>)?.mapNotNull {
                        runCatching { Villager.Profession.valueOf(it as String) }.getOrNull()
                    }?.toMutableSet() ?: mutableSetOf()
                    professionTrades[uuid] = profList
                }
            }
        } catch (ex: Exception) {
            plugin.logger.warning("Failed to load trader data: ${ex.message}")
        }
    }

    /**
     * Get the name of the player who won the heart (if any)
     */
    fun getWinnerName(): String? {
        return heartWinner?.let { uuid ->
            Bukkit.getOfflinePlayer(uuid).name
        }
    }

    /**
     * Check if someone has already claimed the heart
     */
    fun isHeartClaimed(): Boolean = heartWinner != null
}