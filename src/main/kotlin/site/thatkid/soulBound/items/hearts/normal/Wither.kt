package site.thatkid.soulBound.items.hearts.normal

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.WitherSkull
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import site.thatkid.soulBound.hearts.ActiveHearts
import site.thatkid.soulBound.hearts.TrustRegistry
import site.thatkid.soulBound.items.Heart
import site.thatkid.soulBound.items.ItemCreator
import java.util.UUID

object Wither : Heart(), Listener {
    private val plugin: JavaPlugin
        get() = JavaPlugin.getProvidingPlugin(Wither::class.java)

    private val cooldowns = mutableMapOf<UUID, Long>()
    val cooldownTime = 120 * 1000L // 2 minutes in milliseconds
    val blownUpBy = mutableMapOf<UUID, UUID>()

    override val key: NamespacedKey
        get() = NamespacedKey(plugin, "wither")

    override fun createItem(): ItemStack {
        return ItemCreator.itemCreator(13)
    }

    override fun constantEffect(player: Player) {
        return
    }

    @EventHandler
    fun onPlayerHit(event: EntityDamageByEntityEvent) {
        val attacker = event.damager
        val entity = event.entity

        if (attacker !is Player || entity !is Player) return
        if (ActiveHearts.getHearts(attacker.uniqueId).contains(Wither)) {
            if (Math.random() >= 0.1) return // 10% chance
            if (TrustRegistry.getTrusted(attacker.uniqueId).contains(entity.uniqueId)) return
            plugin.logger.info("Wither Heart hit event: Inflicting Wither I on ${entity.name} by ${attacker.name}")
            entity.addPotionEffect(PotionEffect(PotionEffectType.WITHER, 20 * 10, 0))
            attacker.sendMessage("§aYou inflicted Wither I on ${entity.name}!")
        }
    }

    
    override fun specialEffect(player: Player) {
        if (cooldowns.containsKey(player.uniqueId) && System.currentTimeMillis() < cooldowns[player.uniqueId]!!) {
            player.sendMessage("§cYou must wait before using Wither Blast again.")
            return
        }

        val direction = player.location.direction
        val numHeads = 5 // Number of wither heads to shoot - this one is so obvious
        val spacing = 1 // Distance between heads duh - then again that isn't really a duh

        for (i in 0 until numHeads) {
            val location = player.location.clone().add(0.0, 1.0, 0.0)
            val head = player.world.spawnEntity(location, EntityType.WITHER_SKULL) as WitherSkull
            head.isCharged = true
            head.yield = 0f
            head.shooter = player
            head.velocity = direction.clone().multiply(1.5 + i * spacing)
        }

        cooldowns[player.uniqueId] = System.currentTimeMillis() + cooldownTime
        player.sendMessage("§aWither Blast unleashed!")
    }

//    override fun checkProgress(player: Player): String {
//        val tracker = HeartRegistry.witherTracker
//        val uuid = player.uniqueId
//
//        if (tracker.isHeartClaimed()) {
//            return if (tracker.hasReceived(uuid)) {
//                "§8Wither Heart §8| §aUnlocked by you"
//            } else {
//                val winner = tracker.getWinnerName() ?: "another player"
//                "§8Wither Heart §8| §cAlready claimed by $winner"
//            }
//        }
//
//        val kills = tracker.getKills(uuid)
//        return "§8Wither Heart Progress: §f$kills§7/§f7 Wither kills"
//    }
//


    override fun clearCooldown(playerId: UUID) {
        cooldowns.remove(playerId)
    }

    override fun getCooldown(playerId: UUID): Long {
        return cooldowns[playerId] ?: 0L
    }
}