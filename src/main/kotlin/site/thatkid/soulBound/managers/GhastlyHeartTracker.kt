package site.thatkid.soulBound.managers

import com.google.gson.GsonBuilder
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.plugin.java.JavaPlugin
import site.thatkid.soulBound.items.hearts.Ghastly
import java.io.File
import java.util.UUID

class GhastlyHeartTracker(
    private val plugin: JavaPlugin
) : HeartTracker(plugin, Ghastly, killsRequired = -1) {

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val dataFile = File(plugin.dataFolder, "ghastly_heart.json")

    private var globallyReceived = false
    private var returnDone = false
    private var overworldKillDone = false
    private var recipient: UUID? = null

    override fun enable() {
        super.enable()
        load()
        plugin.logger.info("[GhastlyHeartTracker] Enabled – tracking two advancements")
    }

    override fun disable() {
        super.disable()
        save()
        plugin.logger.info("[GhastlyHeartTracker] Disabled – state saved")
    }

    @EventHandler
    override fun onPlayerKill(e: PlayerDeathEvent) {
        // No-op – this tracker uses advancement-based unlock
    }

    @EventHandler
    fun onReturnToSender(e: PlayerAdvancementDoneEvent) {
        if (e.advancement.key.key != "nether/return_to_sender") return
        if (returnDone) return

        returnDone = true
        plugin.logger.info("[GhastlyHeartTracker] Return to Sender done by ${e.player.name}")
        attemptGiveHeart(e.player.uniqueId)
        save()
    }

    @EventHandler
    fun onUneasyAlliance(e: PlayerAdvancementDoneEvent) {
        if (e.advancement.key.key != "nether/uneasy_alliance") return
        if (overworldKillDone) return

        overworldKillDone = true
        plugin.logger.info("[GhastlyHeartTracker] Overworld Ghast kill done by ${e.player.name}")
        attemptGiveHeart(e.player.uniqueId)
        save()
    }

    private fun attemptGiveHeart(uuid: UUID) {
        if (!returnDone || !overworldKillDone || globallyReceived) return
        giveHeart(uuid)
    }

    override fun giveHeart(uuid: UUID) {
        if (globallyReceived) return

        globallyReceived = true
        recipient = uuid
        super.giveHeart(uuid)

        Bukkit.getPlayer(uuid)?.let { player ->
            player.sendMessage("")
            player.sendMessage("§5§l=== GHASTLY HEART UNLOCKED ===")
            player.sendMessage("§7You reflected a ghast's fireball back!")
            player.sendMessage("§7And you dragged one into the Overworld to finish it off!")
            player.sendMessage("§7You've earned the favor of the Nether's spirits.")
            player.sendMessage("§5§l===============================")
            player.sendMessage("")

            player.playSound(player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f)
            player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f)

            Bukkit.broadcast(
                Component.text(
                    "§d${player.name} §7has earned the §5§lGhastly Heart§7 by completing both Return to Sender and the Overworld Ghast Kill!"
                )
            )
        }

        save()
    }

    private fun save() {
        val data = mutableMapOf<String, Any>(
            "globallyReceived" to globallyReceived,
            "returnDone" to returnDone,
            "overworldKillDone" to overworldKillDone
        )
        recipient?.let { data["recipient"] = it.toString() }
        dataFile.parentFile?.mkdirs()
        try {
            dataFile.writeText(gson.toJson(data))
        } catch (ex: Exception) {
            plugin.logger.warning("[GhastlyHeartTracker] Failed to save: ${ex.message}")
        }
    }

    private fun load() {
        if (!dataFile.exists()) return
        try {
            @Suppress("UNCHECKED_CAST")
            val data = gson.fromJson(dataFile.readText(), Map::class.java) as Map<String, Any>
            globallyReceived  = data["globallyReceived"]  as? Boolean ?: false
            returnDone        = data["returnDone"]        as? Boolean ?: false
            overworldKillDone = data["overworldKillDone"] as? Boolean ?: false
            recipient         = (data["recipient"] as? String)?.let(UUID::fromString)
        } catch (ex: Exception) {
            plugin.logger.warning("[GhastlyHeartTracker] Failed to load data: ${ex.message}")
        }
    }
}
