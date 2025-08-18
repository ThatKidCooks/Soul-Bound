package site.thatkid.soulBound.managers

import com.google.gson.GsonBuilder
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.BrewEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionType
import site.thatkid.soulBound.items.hearts.Wise
import java.io.File
import java.util.*

class WiseHeartTracker(private val plugin: JavaPlugin)
    : HeartTracker(plugin, Wise, killsRequired = -1) {

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val dataFile = File(plugin.dataFolder, "wise_heart.json")

    // Track per-player progress
    val playerBrewedPotions = mutableMapOf<UUID, MutableSet<PotionType>>()
    val playerPotionKills = mutableMapOf<UUID, Int>()
    var globallyReceived = false
    var recipient: UUID? = null

    // All brewable potion types (excluding UNCRAFTABLE and WATER)
    val allBrewablePotions = PotionType.entries
        .filterNot { potion ->
            potion == PotionType.WATER ||
                    potion.name.startsWith("STRONG_") ||
                    potion.getPotionEffects().isEmpty()
        }
        .toSet()


    override fun enable() {
        super.enable()
        load()
        plugin.logger.info("[WiseHeartTracker] Enabled – tracking potion brewing and PvP kills under effects")
    }

    override fun disable() {
        super.disable()
        save()
        plugin.logger.info("[WiseHeartTracker] Disabled – saved ${playerBrewedPotions.size} players' progress")
    }

    override fun onPlayerKill(e: PlayerDeathEvent) {
        if (globallyReceived) return

        val killer = e.entity.killer ?: return
        val victim = e.entity

        // Don't count self-kills or non-player kills
        if (killer.uniqueId == victim.uniqueId || victim !is Player) return

        // Check if killer has any active potion effects
        if (killer.activePotionEffects.isEmpty()) return

        val killerUuid = killer.uniqueId
        val currentKills = playerPotionKills.getOrDefault(killerUuid, 0) + 1
        playerPotionKills[killerUuid] = currentKills

        // Progress messages for PvP kills
        when (currentKills) {
            1 -> killer.sendMessage(
                Component.text("§5[Wise Heart] §7First PvP kill under potion effects! §f$currentKills§7/§f5")
            )
            2, 3, 4 -> killer.sendMessage(
                Component.text("§5[Wise Heart] §7PvP kills under effects: §f$currentKills§7/§f5")
            )
            5 -> killer.sendMessage(
                Component.text("§5[Wise Heart] §a✓ PvP requirement complete! §f5§7/§f5 kills under potion effects")
            )
        }

        checkCompletion(killerUuid)
        save()
    }

    @EventHandler
    fun onBrew(event: BrewEvent) {
        if (globallyReceived) return

        // Get the player who initiated the brewing
        val brewer = event.contents.holder?.location?.world?.getNearbyEntities(
            event.contents.holder!!.location, 5.0, 5.0, 5.0
        )?.filterIsInstance<Player>()?.firstOrNull() ?: return

        val uuid = brewer.uniqueId

        // Check all 3 brewing slots for new potions
        for (i in 0..2) {
            val result = event.results[i] ?: continue
            if (result.type != Material.POTION && result.type != Material.SPLASH_POTION && result.type != Material.LINGERING_POTION) continue

            val meta = result.itemMeta as? PotionMeta ?: continue
            val potionType = meta.basePotionData?.type ?: continue

            // Skip water bottles and uncraftable potions
            if (potionType == PotionType.WATER || !allBrewablePotions.contains(potionType)) continue

            val brewedSet = playerBrewedPotions.getOrPut(uuid) { mutableSetOf() }
            if (brewedSet.add(potionType)) {
                val totalBrewed = brewedSet.size
                val totalRequired = allBrewablePotions.size

                when {
                    totalBrewed % 5 == 0 || totalBrewed in setOf(1, 2, 3) -> {
                        brewer.sendMessage(
                            Component.text("§5[Wise Heart] §7Unique potions brewed: §f$totalBrewed§7/§f$totalRequired")
                        )
                    }
                    totalBrewed == totalRequired -> {
                        brewer.sendMessage(
                            Component.text("§5[Wise Heart] §a✓ Brewing requirement complete! All potions brewed!")
                        )
                    }
                    else -> {
                        brewer.sendMessage(
                            Component.text("§5Potion Progress: §f$totalBrewed§7/§f$totalRequired")
                        )
                    }
                }

                checkCompletion(uuid)
            }
        }

        save()
    }

    private fun checkCompletion(uuid: UUID) {
        if (globallyReceived) return

        val brewedPotions = playerBrewedPotions[uuid]?.size ?: 0
        val pvpKills = playerPotionKills[uuid] ?: 0

        val brewingComplete = brewedPotions >= allBrewablePotions.size
        val pvpComplete = pvpKills >= 5

        if (brewingComplete && pvpComplete) {
            giveHeart(uuid)
        }
    }

    override fun giveHeart(uuid: UUID) {
        if (globallyReceived) return
        globallyReceived = true
        recipient = uuid
        super.giveHeart(uuid)

        // Clear progress for all players
        playerBrewedPotions.clear()
        playerPotionKills.clear()

        val player = Bukkit.getPlayer(uuid)
        if (player?.isOnline == true) {
            player.sendMessage(Component.text(""))
            player.sendMessage(Component.text("§5§l=== WISE HEART UNLOCKED ==="))
            player.sendMessage(Component.text("§7You mastered all potions and proved your combat prowess!"))
            player.sendMessage(Component.text("§7Ancient alchemical wisdom flows through you."))
            player.sendMessage(Component.text("§5§l============================="))
            player.sendMessage(Component.text(""))

            player.playSound(player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f)
            player.playSound(player.location, Sound.BLOCK_BREWING_STAND_BREW, 1f, 0.8f)
            player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f)
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.0f)

            // 📢 Broadcast to all players
            Bukkit.broadcast(
                Component.text("§d${player.name} §7has unlocked the §5§lWise Heart§7 by mastering all potions and achieving §f5 PvP kills§7 under potion effects!")
            )
        }
        save()
    }

    override fun save() {
        val data = mutableMapOf<String, Any>()
        data["globallyReceived"] = globallyReceived
        recipient?.let { data["recipient"] = it.toString() }

        // Save brewed potions per player
        val brewingData = mutableMapOf<String, List<String>>()
        playerBrewedPotions.forEach { (uuid, potions) ->
            brewingData[uuid.toString()] = potions.map { it.name }
        }
        data["brewedPotions"] = brewingData

        // Save PvP kills per player
        val killData = mutableMapOf<String, Int>()
        playerPotionKills.forEach { (uuid, kills) ->
            killData[uuid.toString()] = kills
        }
        data["potionKills"] = killData

        dataFile.writeText(gson.toJson(data))
    }

    private fun load() {
        if (!dataFile.exists()) return
        try {
            @Suppress("UNCHECKED_CAST")
            val data = gson.fromJson(dataFile.readText(), Map::class.java) as Map<String, Any>

            globallyReceived = data["globallyReceived"] as? Boolean ?: false
            recipient = (data["recipient"] as? String)?.let(UUID::fromString)

            // Load brewed potions
            (data["brewedPotions"] as? Map<String, List<String>>)?.forEach { (uuidStr, potionNames) ->
                val uuid = UUID.fromString(uuidStr)
                val potions = potionNames.mapNotNull {
                    try { PotionType.valueOf(it) } catch (e: Exception) { null }
                }.toMutableSet()
                if (potions.isNotEmpty()) {
                    playerBrewedPotions[uuid] = potions
                }
            }

            // Load PvP kills
            (data["potionKills"] as? Map<String, Any>)?.forEach { (uuidStr, killCount) ->
                val uuid = UUID.fromString(uuidStr)
                val kills = (killCount as? Number)?.toInt() ?: 0
                if (kills > 0) {
                    playerPotionKills[uuid] = kills
                }
            }
        } catch (ex: Exception) {
            plugin.logger.warning("[WiseHeartTracker] Failed to load data: ${ex.message}")
        }
    }
}