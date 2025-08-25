package site.thatkid.soulBound.items.hearts.rare

import net.kyori.adventure.text.Component
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import site.thatkid.soulBound.hearts.TrustRegistry
import site.thatkid.soulBound.items.Heart
import site.thatkid.soulBound.items.ItemCreator
import java.util.UUID

object Wise : Heart() {
    private val plugin : JavaPlugin
        get() = JavaPlugin.getProvidingPlugin(Wise::class.java)

    private val cooldowns = mutableMapOf<UUID, Long>()
    private val cooldownAmount = 180 * 1000L // 3 minutes in milliseconds

    override val key: NamespacedKey = NamespacedKey(plugin, "wise")

    override fun createItem(): ItemStack {
        return ItemCreator.itemCreator(12)
    }

    override fun constantEffect(player: Player) {
        player.addPotionEffect(PotionEffect(PotionEffectType.HEALTH_BOOST, 50, 0, true, false))
    }

    override fun specialEffect(player: Player) {
        val playerId = player.uniqueId
        val currentTime = System.currentTimeMillis()
        val cooldownTime = cooldowns[playerId] ?: 0L

        if (currentTime - cooldownTime < cooldownAmount) {
            val remaining = (cooldownAmount - (currentTime - cooldownTime)) / 1000
            player.sendMessage(Component.text("§cYou must wait ${remaining} before using this ability again."))
            return
        }

        // Apply the player's effects
        player.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 10 * 20, 1, true, false))
        player.addPotionEffect(PotionEffect(PotionEffectType.ABSORPTION, 10 * 20, 1, true, false))
        player.world.spawnParticle(Particle.HAPPY_VILLAGER, player.location, 100)
        player.world.playSound(player.location, Sound.ENTITY_VILLAGER_WORK_LIBRARIAN, 1f, 1f)
        player.world.playSound(player.location, Sound.ENTITY_VILLAGER_WORK_LIBRARIAN, 1f, 1f)
        player.world.playSound(player.location, Sound.ENTITY_VILLAGER_WORK_LIBRARIAN, 1f, 1f)

        // Apply the other player's effects
        val radius = 30.0

        for (entity in player.world.getNearbyEntities(player.location, radius, radius, radius)) {
            if (entity != player && entity is LivingEntity) {
                if (entity is Player && !TrustRegistry.getTrusted(playerId).contains(entity.uniqueId)) {
                    entity.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 30 * 20, 1, true, false))
                    entity.sendMessage(Component.text("§cYou have been exposed by ${player.name}!"))
                }
                else if (!TrustRegistry.getTrusted(playerId).contains(entity.uniqueId)) {
                    entity.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 30 * 20, 1, true, false))
                }
            }
        }

        // Tell the player how many players were affected
        val affectedCount = player.world.getNearbyEntities(player.location, radius, radius, radius).count {
            it != player && !TrustRegistry.getTrusted(playerId).contains(it.uniqueId) && it is Player
        }

        player.sendMessage(Component.text("§aYou can tell that ${affectedCount} players are around you!"))
        for (entity in player.world.getNearbyEntities(player.location, radius, radius, radius)) {
            if (entity is Player && TrustRegistry.getTrusted(playerId).contains(entity.uniqueId)) {
                entity.sendMessage(Component.text("The caster ${player.name} has found that ${affectedCount} players are around them!"))
            }
        }

        // Send a message to the player
        player.sendMessage(Component.text("§aYou feel a surge of wisdom!"))

        // Set the cooldown
        cooldowns[playerId] = currentTime
    }

//    override fun checkProgress(player: Player): String {
//        val tracker = HeartRegistry.wiseTracker
//        val uuid = player.uniqueId
//
//        if (tracker.globallyReceived) {
//            return if (tracker.recipient == uuid) {
//                "§5Wise Heart §8| §aUnlocked by you"
//            } else {
//                "§5Wise Heart §8| §cAlready claimed by another player"
//            }
//        }
//
//        val brewed = tracker.playerBrewedPotions[uuid]?.size ?: 0
//        val kills = tracker.playerPotionKills[uuid] ?: 0
//        val totalPotions = tracker.allBrewablePotions.size
//
//        val brewStatus = if (brewed >= totalPotions) {
//            "§a✓ Brewing complete"
//        } else {
//            "§7Brewed §f$brewed§7/§f$totalPotions potions"
//        }
//
//        val killStatus = if (kills >= 5) {
//            "§a✓ PvP complete"
//        } else {
//            "§7Kills under potion effects: §f$kills§7/§f5"
//        }
//
//        return "§5Wise Heart Progress:\n$brewStatus\n$killStatus"
//    }


    override fun clearCooldown(playerId: UUID) {
        cooldowns.remove(playerId)
    }

    override fun getCooldown(playerId: UUID): Long {
        return cooldowns[playerId] ?: 0L
    }
}