package site.thatkid.soulBound.managers

import com.google.gson.GsonBuilder
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.block.Biome
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.plugin.java.JavaPlugin
import site.thatkid.soulBound.items.hearts.Aquatic
import java.io.File
import java.util.*
import kotlin.math.sqrt

class AquaticHeartTracker(private val plugin: JavaPlugin)
    : HeartTracker(plugin, Aquatic, killsRequired = 5000) {

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val dataFile = File(plugin.dataFolder, "aquatic_heart.json")

    private val distances = mutableMapOf<UUID, Double>()
    private val receivedHeart = mutableSetOf<UUID>()
    private val lastLocations = mutableMapOf<UUID, Location>()
    private var globallyReceived = false

    private val oceanBiomes = setOf(
        Biome.OCEAN,
        Biome.DEEP_OCEAN,
        Biome.COLD_OCEAN,
        Biome.DEEP_COLD_OCEAN,
        Biome.FROZEN_OCEAN,
        Biome.DEEP_FROZEN_OCEAN,
        Biome.LUKEWARM_OCEAN,
        Biome.DEEP_LUKEWARM_OCEAN,
        Biome.WARM_OCEAN,
    )

    override fun enable() {
        super.enable()
        load()
        plugin.logger.info("[AquaticHeartTracker] Enabled – tracking ocean swim distance")
    }

    override fun disable() {
        super.disable()
        save()
        plugin.logger.info("[AquaticHeartTracker] Disabled – saved ${distances.size} players' distances")
    }

    override fun onPlayerKill(e: org.bukkit.event.entity.PlayerDeathEvent) { /* no-op */ }

    @EventHandler
    fun onSwim(event: PlayerMoveEvent) {
        if (globallyReceived) return

        val player = event.player
        val uuid = player.uniqueId

        if (receivedHeart.contains(uuid)) return
        if (!player.isInWater || !player.isSwimming) return

        val biome = player.world.getBiome(player.location)
        if (biome !in oceanBiomes) return

        val lastLoc = lastLocations[uuid] ?: event.from
        val distance = event.to.distance(lastLoc)
        if (distance < 0.5) return // ignore micro movements

        val newTotal = distances.getOrDefault(uuid, 0.0) + distance
        distances[uuid] = newTotal
        lastLocations[uuid] = event.to

        val intDist = newTotal.toInt()
        when (intDist) {
            10, 20, 1000, 2500, 4000, 4900 -> player.sendMessage(
                Component.text("§3[Aquatic Heart] §7Progress: §b$intDist§7/§b5,000")
            )
            5000 -> {
                giveHeart(uuid)
                return
            }
        }

        if (intDist % 500 == 0 && intDist !in setOf(1000, 2500, 4000, 4900)) {
            player.sendActionBar(
                Component.text("§3Swim Progress: §b$intDist§7/§b5,000")
            )
        }

        save()
    }

    override fun giveHeart(uuid: UUID) {
        if (globallyReceived) return
        globallyReceived = true
        super.giveHeart(uuid)
        receivedHeart.add(uuid)
        distances.remove(uuid)

        // Optional: wipe progress tracking for all players
        distances.clear()
        lastLocations.clear()

        val player = Bukkit.getPlayer(uuid)
        if (player?.isOnline == true) {
            player.sendMessage(Component.text(""))
            player.sendMessage(Component.text("§3§l=== AQUATIC HEART UNLOCKED ==="))
            player.sendMessage(Component.text("§7You swam §b5,000 blocks §7in the ocean!"))
            player.sendMessage(Component.text("§7The sea has blessed you."))
            player.sendMessage(Component.text("§3§l=============================="))
            player.sendMessage(Component.text(""))

            player.playSound(player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f)
            player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f)

            // 📢 Broadcast to all players
            Bukkit.broadcast(
                Component.text("§b${player.name} §7has unlocked the §3§lAquatic Heart§7 by swimming §b5,000 blocks§7 in the ocean!")
            )
        }
        save()
    }


    private fun save() {
        val data = mutableMapOf<String, Any>()
        distances.forEach { (uuid, dist) ->
            data[uuid.toString()] = dist
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
                        val dist = (value as? Number)?.toDouble() ?: return@forEach
                        distances[uuid] = dist
                    }
                }
            }
        } catch (ex: Exception) {
            plugin.logger.warning("[AquaticHeartTracker] Failed to load swim data: ${ex.message}")
        }
    }
}
