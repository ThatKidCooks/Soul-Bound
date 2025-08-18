package site.thatkid.soulBound.managers

import com.google.gson.GsonBuilder
import org.bukkit.Bukkit
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.plugin.java.JavaPlugin
import site.thatkid.soulBound.items.hearts.Fire
import java.io.File
import java.util.UUID

class FireHeartTracker(private val plugin: JavaPlugin) : Listener {

  private val gson = GsonBuilder().setPrettyPrinting().create()
  private val dataFile = File(plugin.dataFolder, "fire_heart.json")

  // The single player who has received the heart (null if no one has it yet)
  private var heartWinner: UUID? = null
  private val witherKillers: MutableSet<UUID> = mutableSetOf()

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
    plugin.logger.info("[FireHeartTracker] Enabled – listening for wither kills")
  }

  /**
   * Call from your plugin's onDisable()
   */
  fun disable() {
    save()
    plugin.logger.info("[FireHeartTracker] Disabled – saved wither heart winner")
  }

  @EventHandler
  fun onWitherDeath(e: EntityDeathEvent) {

    if (e.entityType != EntityType.WITHER) return
    val killer = e.entity.killer ?: return

    witherKillers += killer.uniqueId

    // if someone already has the heart, no one else can earn it
    if (heartWinner != null) killer.sendMessage("${killer.name} has already won the Fire Heart. No one else can earn it.")

    // Give the heart to the first player to kill a wither
    killer.inventory.addItem(Fire.createItem())
    killer.sendMessage("§2You are the first to slay a Wither and have earned the §cFire Heart§r§2!")
    Bukkit.broadcastMessage("§a${killer.name} is the first to slay a Wither and earn the §cFire Heart§r§a!")

    heartWinner = killer.uniqueId
    save()
  }

  fun hasKilledWither(playerId: UUID): Boolean = playerId in witherKillers

  fun isHeartClaimed(): Boolean = heartWinner != null

  fun hasReceived(uuid: UUID): Boolean = heartWinner == uuid

  fun getWinnerName(): String? =
    heartWinner?.let { Bukkit.getOfflinePlayer(it).name }

      /**
   * Save winner to JSON
   */
  fun save() {
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
      plugin.logger.warning("Failed to load fire heart data: ${ex.message}")
    }
  }
}