package site.thatkid.soulBound.items.hearts

import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.entity.Ghast
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Monster
import org.bukkit.entity.Phantom
import org.bukkit.entity.Player
import org.bukkit.entity.Slime
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import site.thatkid.soulBound.hearts.TrustRegistry
import site.thatkid.soulBound.items.Heart
import java.util.*

object Warden : Heart() {

    var remaining: Long = 0L
    private val plugin: JavaPlugin = JavaPlugin.getProvidingPlugin(Warden::class.java)

    override val key = NamespacedKey(plugin, "warden")
    private val cooldowns = mutableMapOf<UUID, Long>()
    val cooldownTime = 150 * 1000L // 2.5 minutes in milliseconds
    val obliteratedBy = mutableMapOf<UUID, UUID>()

    override fun createItem(): ItemStack {
        val item = ItemStack(Material.APPLE)
        val meta = item.itemMeta!!
        meta.displayName(Component.text("§1Warden Heart"))
        meta.lore(listOf(
            Component.text("§7Unlock by defeating the"),
            Component.text("§fWarden §7without dying."),
            Component.text(""),
            Component.text("§f✧ §7Nearby mobs tremble in your presence."),
            Component.text("§f✧ §7Darkness creeps into the hearts of enemies."),
            Component.text(""),
            Component.text("§9§lPower — Sonic Pulse"),
            Component.text("§7Unleash a §bSonic Boom §7that damages and knocks back enemies"),
            Component.text("§8Cooldown: 2m 30s")
        ))
        meta.persistentDataContainer.set(key, PersistentDataType.BYTE, 1)
        item.itemMeta = meta
        return item
    }


    override fun constantEffect(player: Player) {
        val trusted = TrustRegistry.getTrusted(player.uniqueId)
        for (entity in player.world.getNearbyEntities(player.location, 8.0, 8.0, 8.0)) {
            if (entity is LivingEntity && entity != player) {
                if (entity is Monster || entity is Slime || entity is Ghast || entity is Phantom || entity is Player) {
                    for (other in player.world.players) {
                        if (other != player && other.location.distance(other.location) < 32 && !trusted.contains(other.uniqueId)) {
                            other.playSound(other.location, Sound.ENTITY_WARDEN_ANGRY, 1f, 1f)
                        }
                    }
                    if (!trusted.contains(entity.uniqueId)) {
                        if (!player.hasPotionEffect(PotionEffectType.DARKNESS)) {
                            player.addPotionEffect(PotionEffect(PotionEffectType.DARKNESS, 40, 0, false, true))
                        }
                    }
                }
            }
        }
    }

    override fun specialEffect(player: Player) {
        val now = System.currentTimeMillis()

        if (cooldowns[player.uniqueId]?.let { now - it < cooldownTime } == true) {
            remaining = (cooldownTime - (now - cooldowns[player.uniqueId]!!)) / 1000
            player.sendMessage(Component.text("§cWarden pulse on cooldown! Wait $remaining seconds."))
            return
        }

        cooldowns[player.uniqueId] = now

        val direction = player.location.direction.normalize()
        val start = player.eyeLocation.clone()
        val trusted = TrustRegistry.getTrusted(player.uniqueId)
        val hitEntities = mutableSetOf<UUID>()

        object : BukkitRunnable() {
            var distance = 0.0

            override fun run() {
                distance += 1.0
                if (distance > 20) {
                    cancel()
                    return
                }

                val point = start.clone().add(direction.clone().multiply(distance))
                player.world.spawnParticle(Particle.SONIC_BOOM, point, 1, 0.0, 0.0, 0.0, 0.0)
                player.world.playSound(point, Sound.ENTITY_WARDEN_SONIC_BOOM, 2f, 1f)

                for (entity in point.world.getNearbyEntities(point, 1.5, 1.5, 1.5)) {
                    if (entity == player || entity !is LivingEntity || entity.uniqueId in hitEntities) continue
                    if (trusted.contains(entity.uniqueId)) continue
                    if (player.gameMode != GameMode.SURVIVAL || player.gameMode != GameMode.ADVENTURE) continue

                    val damage = 15.0 // 7.5 hearts

                    val newHealth = (entity.health - damage).coerceAtLeast(0.0)
                    entity.health = newHealth

                    if (entity is Player) {
                        obliteratedBy[entity.uniqueId] = player.uniqueId
                    }

                    val knockback = entity.location.toVector().subtract(player.location.toVector()).normalize().multiply(1.5)
                    knockback.y = 0.5
                    entity.velocity = knockback
                    hitEntities.add(entity.uniqueId)
                }
            }
        }.runTaskTimer(plugin, 0L, 1L)

        player.sendMessage(Component.text("§bYou unleashed a §lSonic Boom§r§b!"))
        player.world.playSound(player.location, Sound.ENTITY_WARDEN_SONIC_BOOM, 2f, 0.8f)
    }

    override fun clearCooldown(uuid: UUID) {
        cooldowns.remove(uuid)
    }

    override fun getCooldown(playerId: UUID): Long {
        return cooldowns[playerId] ?: 0L
    }
}
