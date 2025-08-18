package site.thatkid.soulBound.managers

import com.google.gson.GsonBuilder
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.entity.IronGolem
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.plugin.java.JavaPlugin
import site.thatkid.soulBound.items.hearts.Golem
import java.io.File
import java.util.*

class GolemHeartTracker(private val plugin: JavaPlugin)
    : HeartTracker(plugin, Golem, killsRequired = 100) {

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val dataFile = File(plugin.dataFolder, "golem_heart.json")

    private val kills = mutableMapOf<UUID, Int>()
    private val receivedHeart = mutableSetOf<UUID>()
    private var globallyReceived = false

    override fun enable() {
        super.enable()
        load()
        plugin.logger.info("[GolemHeartTracker] Enabled – tracking iron golem kills")
    }

    override fun disable() {
        super.disable()
        save()
        plugin.logger.info("[GolemHeartTracker] Disabled – saved ${kills.size} players' kill counts")
    }

    override fun onPlayerKill(e: org.bukkit.event.entity.PlayerDeathEvent) { /* no-op */ }

    @EventHandler
    fun onGolemKill(event: EntityDeathEvent) {
        if (globallyReceived) return
        
        val entity = event.entity
        if (entity.type != EntityType.IRON_GOLEM) return
        
        val golem = entity as IronGolem
        
        // Only count naturally spawned golems (not player-built ones)
        if (golem.isPlayerCreated) return
        
        val killer = event.entity.killer ?: return
        val uuid = killer.uniqueId
        
        if (receivedHeart.contains(uuid)) return
        
        val newKills = kills.getOrDefault(uuid, 0) + 1
        kills[uuid] = newKills
        
        when (newKills) {
            1, 5, 10, 25, 50, 75, 90, 95, 99 -> killer.sendMessage(
                Component.text("§7[Golem Heart] §7Progress: §f$newKills§7/§f100 §7iron golems killed")
            )
            100 -> {
                giveHeart(uuid)
                return
            }
        }
        
        if (newKills % 10 == 0 && newKills !in setOf(10, 50, 90)) {
            killer.sendActionBar(
                Component.text("§7Golem Kills: §f$newKills§7/§f100")
            )
        }
        
        save()
    }

    override fun giveHeart(uuid: UUID) {
        if (globallyReceived) return
        globallyReceived = true
        super.giveHeart(uuid)
        receivedHeart.add(uuid)
        kills.remove(uuid)

        // Optional: wipe progress tracking for all players
        kills.clear()

        val player = Bukkit.getPlayer(uuid)
        if (player?.isOnline == true) {
            player.sendMessage(Component.text(""))
            player.sendMessage(Component.text("§7§l=== GOLEM HEART UNLOCKED ==="))
            player.sendMessage(Component.text("§7You defeated §f100 iron golems§7!"))
            player.sendMessage(Component.text("§7Iron flows through your veins."))
            player.sendMessage(Component.text("§7§l============================="))
            player.sendMessage(Component.text(""))

            player.playSound(player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f)
            player.playSound(player.location, Sound.ENTITY_IRON_GOLEM_DEATH, 1f, 0.8f)
            player.playSound(player.location, Sound.BLOCK_ANVIL_PLACE, 1f, 1.2f)

            // 📢 Broadcast to all players
            Bukkit.broadcast(
                Component.text("§f${player.name} §7has unlocked the §7§lGolem Heart§7 by defeating §f100 iron golems§7!")
            )
        }
        save()
    }

    override fun getKills(uuid: UUID): Int = kills[uuid] ?: 0
    fun hasReceived(uuid: UUID): Boolean = receivedHeart.contains(uuid)
    fun isGloballyReceived(): Boolean = globallyReceived
    fun getRequired(): Int = 100

    override fun save() {
        val data = mutableMapOf<String, Any>()
        kills.forEach { (uuid, killCount) ->
            data[uuid.toString()] = killCount
        }
        data["__received__"] = receivedHeart.map(UUID::toString)
        data["__globallyReceived__"] = globallyReceived
        dataFile.writeText(gson.toJson(data))
    }

    private fun load() {
        if (!dataFile.exists()) return
        try {
            @Suppress("UNCHECKED_CAST")
            val tree = gson.fromJson(dataFile.readText(), Map::class.java) as Map<String, Any>
            tree.forEach { (key, value) ->
                when (key) {
                    "__received__" -> {
                        (value as? List<*>)?.forEach {
                            (it as? String)?.let { receivedHeart.add(UUID.fromString(it)) }
                        }
                    }
                    "__globallyReceived__" -> {
                        globallyReceived = value as? Boolean ?: false
                    }
                    else -> {
                        val uuid = UUID.fromString(key)
                        val killCount = (value as? Number)?.toInt() ?: return@forEach
                        kills[uuid] = killCount
                    }
                }
            }
        } catch (ex: Exception) {
            plugin.logger.warning("[GolemHeartTracker] Failed to load golem kill data: ${ex.message}")
        }
    }
}