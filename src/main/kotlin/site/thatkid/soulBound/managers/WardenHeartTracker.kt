package site.thatkid.soulBound.managers

import com.google.gson.GsonBuilder
import org.bukkit.Bukkit
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.plugin.java.JavaPlugin
import site.thatkid.soulBound.items.hearts.Warden
import java.io.File
import java.util.UUID

class WardenHeartTracker(private val plugin: JavaPlugin) : Listener {

  private val gson = GsonBuilder().setPrettyPrinting().create()
  private val dataFile = File(plugin.dataFolder, "warden_heart.json")

  // The single player who has received the heart (null if no one has it yet)
  private var heartWinner: UUID? = null

  /**
   * Call from your plugin's onEnable()
   */
  fun enable() {
    // ensure plugin folder exists
    plugin.dataFolder.mkdirs()
    // load saved data
    load()
    // register this listener
    plugin.server.pluginManager.registerEvents(this, plugin)
    plugin.logger.info("[WardenHeartTracker] Enabled – listening for warden kills")
  }

  /**
   * Call from your plugin's onDisable()
   */
  fun disable() {
    save()
    plugin.logger.info("[WardenHeartTracker] Disabled – saved warden heart winner")
  }

  @EventHandler
  fun onWardenDeath(e: EntityDeathEvent) {
    // if someone already has the heart, no one else can earn it
    if (heartWinner != null) return

    if (e.entityType != EntityType.WARDEN) return
    val killer = e.entity.killer ?: return

    // Give the heart to the first player to kill a warden
    killer.inventory.addItem(Warden.createItem())
    killer.sendMessage("§2You are the first to slay a Warden and have earned the §lWarden Heart§r§2!")
    Bukkit.broadcastMessage("§a${killer.name} is the first to slay a Warden and earn the §lWarden Heart§r§a!")

    heartWinner = killer.uniqueId
    save()
  }

  /**
   * Save winner to JSON
   */
  private fun save() {
    val content = mutableMapOf<String, Any>()
    heartWinner?.let { content["winner"] = it.toString() }
    dataFile.writeText(gson.toJson(content))
  }

  /**
   * Load winner from JSON
   */
  private fun load() {
    if (!dataFile.exists()) return
    try {
      val tree = gson.fromJson(dataFile.readText(), Map::class.java) as Map<String, Any>
      (tree["winner"] as? String)?.let {
        heartWinner = UUID.fromString(it)
      }
    } catch (ex: Exception) {
      plugin.logger.warning("Failed to load warden heart data: ${ex.message}")
    }
  }
}