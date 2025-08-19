package site.thatkid.soulBound.managers

import com.google.gson.GsonBuilder
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import site.thatkid.soulBound.hearts.ActiveHearts
import site.thatkid.soulBound.items.Heart
import java.io.File
import java.util.UUID

abstract class HeartTracker(private val plugin: JavaPlugin, protected val heart: Heart, private val killsRequired: Int) : Listener {

  private val gson = GsonBuilder().setPrettyPrinting().create()
  private val dataFile = File(plugin.dataFolder, "${heart.key.key}.json")

  private val killTracker: MutableMap<UUID, MutableSet<UUID>> = mutableMapOf()
  protected var owner: UUID? = null

  protected open fun giveHeart(killerId: UUID) {
    owner = killerId
    Bukkit.getPlayer(killerId)?.let {
      it.inventory.addItem(heart.createItem())
      Bukkit.broadcastMessage(
        "§a${it.name} has obtained the §l${heart.key.key.capitalize()} Heart§r§a!"
      )
    }
    startHeartCheckLoop()
  }

  open fun getKills(playerId: UUID): Int {
    return killTracker[playerId]?.size ?: 0
  }

  fun getKillsRequired(): Int = killsRequired

  open fun enable() {
    plugin.dataFolder.mkdirs()
    load()
    plugin.server.pluginManager.registerEvents(this, plugin)
  }

  open fun disable() {
    save()
  }

  @EventHandler
  protected open fun onPlayerKill(e: PlayerDeathEvent) {
    val killer = e.entity.killer ?: return
    if (owner != null || killer.uniqueId == e.entity.uniqueId) return

    val set = killTracker.getOrPut(killer.uniqueId) { mutableSetOf() }
    set.add(e.entity.uniqueId)
    if (set.size >= killsRequired) {
      giveHeart(killer.uniqueId)
    }
  }

  fun isClaimed(): Boolean = owner != null

  fun isOwner(uuid: UUID): Boolean = owner == uuid

  fun getOwnerName(): String? = owner?.let { Bukkit.getOfflinePlayer(it).name }


  open fun save() {
    val dto = mapOf(
      "owner" to owner?.toString(),
      "tracker" to killTracker.mapValues { it.value.map(UUID::toString) }
    )
    try {
      dataFile.writeText(gson.toJson(dto))
    } catch (ex: Exception) {
      plugin.logger.warning("Could not save ${dataFile.name}: ${ex.message}")
    }
  }

  private fun load() {
    if (!dataFile.exists()) return
    try {
      val tree = gson.fromJson(dataFile.readText(), Map::class.java)
      (tree["owner"] as? String)?.let { owner = UUID.fromString(it) }
      (tree["tracker"] as? Map<*, *>)?.forEach { (k, v) ->
        val killerId = UUID.fromString(k as String)
        @Suppress("UNCHECKED_CAST")
        val victims = (v as List<String>).map(UUID::fromString).toMutableSet()
        killTracker[killerId] = victims
      }
    } catch (ex: Exception) {
      plugin.logger.warning("Failed to load ${dataFile.name}: ${ex.message}")
    }
  }

  private fun startHeartCheckLoop() {
    object : BukkitRunnable() {
      override fun run() {
        val id = owner ?: return cancel()
        val player = Bukkit.getPlayer(id)

        val anyoneHasHeart = Bukkit.getOnlinePlayers().any {
          it.inventory.containsAtLeast(heart.createItem(), 1)
        }
        if (!anyoneHasHeart) {
          killTracker.clear()
          owner = null
          save()
          Bukkit.broadcastMessage(
            "§cThe ${heart.key.key.capitalize()} Heart has been lost! Progress has been reset."
          )
          cancel()
        }
      }
    }.runTaskTimer(plugin, 20L * 10, 20L * 10)
  }
}
