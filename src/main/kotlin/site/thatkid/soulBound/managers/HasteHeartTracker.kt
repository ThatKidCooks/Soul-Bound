package site.thatkid.soulBound.managers

import com.google.gson.GsonBuilder
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.plugin.java.JavaPlugin
import site.thatkid.soulBound.items.hearts.Haste
import java.io.File
import java.util.*

class HasteHeartTracker(private val plugin: JavaPlugin)
    : HeartTracker(plugin, Haste, killsRequired = 5000)
{
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val dataFile = File(plugin.dataFolder, "haste_heart.json")

    private val deepslateCount = mutableMapOf<UUID, Int>()
    private val receivedHeart = mutableSetOf<UUID>()
    private var globallyReceived = false

    private val deepslateTypes = setOf(
        Material.DEEPSLATE,
        Material.COBBLED_DEEPSLATE,
        Material.DEEPSLATE_COAL_ORE,
        Material.DEEPSLATE_IRON_ORE,
        Material.DEEPSLATE_COPPER_ORE,
        Material.DEEPSLATE_GOLD_ORE,
        Material.DEEPSLATE_REDSTONE_ORE,
        Material.DEEPSLATE_LAPIS_ORE,
        Material.DEEPSLATE_DIAMOND_ORE,
        Material.DEEPSLATE_EMERALD_ORE
    )

    override fun enable() {
        super.enable()
        plugin.logger.info("[HasteHeartTracker] Enabled – tracking deepslate mining")
        load()
    }

    override fun disable() {
        super.disable()
        plugin.logger.info("[HasteHeartTracker] Disabled – saved ${deepslateCount.size} players' mining progress")
    }

    override fun onPlayerKill(e: org.bukkit.event.entity.PlayerDeathEvent) { /* no-op */ }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        if (globallyReceived) return
        if (!deepslateTypes.contains(event.block.type)) return

        val player = event.player
        val uuid = player.uniqueId
        if (receivedHeart.contains(uuid)) return

        val currentCount = deepslateCount.getOrPut(uuid) { 0 } + 1
        deepslateCount[uuid] = currentCount
        save()

        when (currentCount) {
            10 -> player.sendMessage(Component.text("TEST: §6[Haste Heart] §7Progress: §e10§7/§e5,000"))
            1000 -> player.sendMessage(Component.text("§6[Haste Heart] §7Progress: §e1,000§7/§e5,000"))
            2500 -> player.sendMessage(Component.text("§6[Haste Heart] §7Progress: §e2,500§7/§e5,000 §7Halfway there!"))
            4000 -> player.sendMessage(Component.text("§6[Haste Heart] §7Progress: §e4,000§7/§e5,000 §7Almost there!"))
            4900 -> player.sendMessage(Component.text("§6[Haste Heart] §7Progress: §e4,900§7/§e5,000 §c100 remaining!"))
            5000 -> {
                giveHeart(uuid)
                return
            }
        }

        if (currentCount > 1000 && currentCount % 100 == 0 && currentCount !in setOf(2500, 4000, 4900)) {
            player.sendActionBar(Component.text("§6Haste Heart Progress: §e$currentCount§7/§e5,000"))
        }

        save()
    }

    override fun giveHeart(uuid: UUID) {
        if (globallyReceived) return
        globallyReceived = true
        super.giveHeart(uuid)
        receivedHeart.add(uuid)
        deepslateCount.remove(uuid)

        deepslateCount.clear()

        val player = plugin.server.getPlayer(uuid)
        if (player?.isOnline == true) {
            player.sendMessage(Component.text(""))
            player.sendMessage(Component.text("§6§l=== HASTE HEART UNLOCKED ==="))
            player.sendMessage(Component.text("§7You have mined §e5,000 deepslate blocks§7!"))
            player.sendMessage(Component.text("§7The power of efficient mining is now yours!"))
            player.sendMessage(Component.text("§6§l==========================="))
            player.sendMessage(Component.text(""))

            player.playSound(player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f)
            player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f)

            val loc = player.location.add(0.0, 1.0, 0.0)
            player.world.spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 30, 1.0, 1.0, 1.0, 0.3)
            player.world.spawnParticle(Particle.FIREWORK, loc, 20, 1.5, 1.5, 1.5, 0.2)

            // 📢 Broadcast to all players
            plugin.server.broadcast(
                Component.text("§e${player.name} §7has unlocked the §6§lHaste Heart§7 by mining §e5,000 deepslate blocks§7!")
            )
        }

        save()
    }

    fun getDeepslateMined(uuid: UUID): Int = deepslateCount[uuid] ?: 0
    fun getRequired(): Int = 5000
    fun hasReceived(uuid: UUID): Boolean = receivedHeart.contains(uuid)
    fun isGloballyReceived(): Boolean = globallyReceived

    override fun save() {
        val data = mutableMapOf<String, Any>()
        deepslateCount.forEach { (uuid, count) ->
            data[uuid.toString()] = count
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
                        val count = (value as? Number)?.toInt() ?: return@forEach
                        deepslateCount[uuid] = count
                    }
                }
            }
        } catch (ex: Exception) {
            plugin.logger.warning("Failed to load haste data: ${ex.message}")
        }
    }
}
