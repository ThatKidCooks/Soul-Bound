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

class WitherHeartTracker(private val plugin: JavaPlugin) : Listener {

  private val gson = GsonBuilder().setPrettyPrinting().create()
  private val dataFile = File(plugin.dataFolder, "wither_heart.json")

  private val kills = mutableMapOf<UUID, Int>()

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
    plugin.logger.info("[WitherHeartTracker] Enabled – listening for wither kills")
  }

  /**
   * Call from your plugin's onDisable()
   */
  fun disable() {
    save()
    plugin.logger.info("[WitherHeartTracker] Disabled – saved wither heart winner")
  }

  fun getKills(uuid: UUID): Int = kills[uuid] ?: 0

  fun hasReceived(uuid: UUID): Boolean = heartWinner == uuid

  fun isHeartClaimed(): Boolean = heartWinner != null

  fun getWinnerName(): String? =
    heartWinner?.let { Bukkit.getOfflinePlayer(it).name }


  @EventHandler
  fun onWitherDeath(e: EntityDeathEvent) {
    // if someone already has the heart, no one else can earn it
    if (heartWinner != null) return

    if (e.entityType != EntityType.WITHER) return
    val killer = e.entity.killer ?: return

    kills[killer.uniqueId] = kills.getOrDefault(killer.uniqueId, 0) + 1

    if (kills[killer.uniqueId] == 7) {

      // Give the heart to the first player to kill a wither
      killer.inventory.addItem(Fire.createItem())
      killer.sendMessage("§2You are the first to slay 7 Withers and have earned the §8Wither Heart§r§2!")
      Bukkit.broadcastMessage("§a${killer.name} is the first to slay 7 Withers and earn the §8Wither Heart§r§a!")

      heartWinner = killer.uniqueId
    }
    save()
  }

  /**
   * Save winner to JSON
   */
  private fun save() {
    val content = mutableMapOf<String, Any>()
    kills.forEach { (uuid, killCount) ->
      content[uuid.toString()] = killCount
      println(content)
    }
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
      tree.forEach { (key, value) ->
        when (key) {
          "winner" -> {
            heartWinner = (value as? String)?.let { UUID.fromString(it) }
          }
          else -> {
            val uuid = UUID.fromString(key)
            val killCount = (value as? Number)?.toInt() ?: return@forEach
            plugin.logger.info("[WitherHeartTracker] Loading with UUID $uuid for $killCount kills")
            kills[uuid] = killCount
            println(kills)
          }
        }

      }
    } catch (ex: Exception) {
      plugin.logger.warning("Failed to load fire heart data: ${ex.message}")
    }
  }
}