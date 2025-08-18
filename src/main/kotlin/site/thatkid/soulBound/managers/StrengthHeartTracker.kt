package site.thatkid.soulBound.managers

import com.google.gson.GsonBuilder
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.plugin.java.JavaPlugin
import site.thatkid.soulBound.items.hearts.Strength
import java.io.File
import java.util.*

class StrengthHeartTracker(private val plugin: JavaPlugin)
    : HeartTracker(plugin, Strength, killsRequired = 10) {

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val dataFile = File(plugin.dataFolder, "strength_heart.json")

    private val killCounts = mutableMapOf<UUID, Int>()
    private var globallyReceived = false
    private var recipient: UUID? = null

    override fun enable() {
        super.enable()
        load()
        plugin.logger.info("[StrengthHeartTracker] Enabled – tracking PvP kills")
    }

    override fun disable() {
        super.disable()
        save()
        plugin.logger.info("[StrengthHeartTracker] Disabled – saved progress")
    }

    override fun onPlayerKill(e: PlayerDeathEvent) {
        if (globallyReceived) return
        val killer = e.entity.killer ?: return
        val uuid = killer.uniqueId

        val currentKills = killCounts.getOrPut(uuid) { 0 } + 1
        killCounts[uuid] = currentKills
        save()

        if (currentKills == 10) {
            giveHeart(uuid)
        } else if (currentKills in setOf(1, 5, 7, 9)) {
            killer.sendMessage("§c[Strength Heart] §7Progress: §e$currentKills§7/§e10")
        }
    }

    override fun getKills(uuid: UUID): Int = killCounts[uuid] ?: 0
    fun getRequired(): Int = 10
    fun hasReceived(uuid: UUID): Boolean = recipient == uuid
    fun isGloballyReceived(): Boolean = globallyReceived

    override fun giveHeart(uuid: UUID) {
        if (globallyReceived) return
        globallyReceived = true
        recipient = uuid
        super.giveHeart(uuid)

        // Wipe all progress tracking
        killCounts.clear()

        val player = Bukkit.getPlayer(uuid)
        if (player?.isOnline == true) {
            player.sendMessage("")
            player.sendMessage("§c§l=== STRENGTH HEART UNLOCKED ===")
            player.sendMessage("§7You defeated §e10 players§7 in combat!")
            player.sendMessage("§7The spirit of battle empowers you.")
            player.sendMessage("§c§l===============================")
            player.sendMessage("")

            player.playSound(player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f)
            player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f)

            // 📢 Broadcast
            Bukkit.broadcast(
                Component.text(
                    "§c${player.name} §7has unlocked the §4§lStrength Heart§7 by defeating §c10 players§7!"
                )
            )
        }

        save()
    }

    override fun save() {
        val data = mutableMapOf<String, Any>()
        killCounts.forEach { (uuid, count) ->
            data[uuid.toString()] = count
        }
        data["__globallyReceived__"] = globallyReceived
        recipient?.let { data["__recipient__"] = it.toString() }
        dataFile.writeText(gson.toJson(data))
    }

    private fun load() {
        if (!dataFile.exists()) return
        try {
            @Suppress("UNCHECKED_CAST")
            val tree = gson.fromJson(dataFile.readText(), Map::class.java) as Map<String, Any>
            tree.forEach { (key, value) ->
                when (key) {
                    "__globallyReceived__" -> globallyReceived = value as? Boolean ?: false
                    "__recipient__" -> recipient = (value as? String)?.let(UUID::fromString)
                    else -> {
                        val uuid = UUID.fromString(key)
                        val count = (value as? Number)?.toInt() ?: return@forEach
                        killCounts[uuid] = count
                    }
                }
            }
        } catch (ex: Exception) {
            plugin.logger.warning("[StrengthHeartTracker] Failed to load data: ${ex.message}")
        }
    }
}
