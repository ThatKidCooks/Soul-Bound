package site.thatkid.soulBound.items.hearts

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
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
import java.util.UUID

object Frozen : Heart(), Listener {
    private val plugin: JavaPlugin = JavaPlugin.getProvidingPlugin(Frozen::class.java)

    private val cooldowns = mutableMapOf<UUID, Long>()
    private var remaining: Long = 0L
    private var cooldownTime = 100 * 1000L // 20 seconds in milliseconds

    override val key: NamespacedKey
        get() = NamespacedKey(plugin, "frozen")

    override fun createItem(): ItemStack {
        val item = ItemStack(Material.APPLE)
        val meta = item.itemMeta!!
        meta.displayName(Component.text("§1Frozen"))
        meta.lore(listOf(
            Component.text("§7Born in the Icy Lakes"),
            Component.text(""),
            Component.text("§f✧ §7Permanent §7Freeze Resistance §7& §7and a 10% chance to"),
            Component.text("§7freeze an entity on hit for §f5 seconds"),
            Component.text(""),
            Component.text("§3§lPower — Frozen Surge"),
            Component.text("§cFreeze all nearby entities making it so they can't jump for §f10 seconds"),
            Component.text("§8Cooldown: 100 seconds")
        ))
        meta.persistentDataContainer.set(key, PersistentDataType.BYTE, 1)
        item.itemMeta = meta
        return item
    }

    override fun constantEffect(player: Player) {
        if (player.isFrozen) {
            player.freezeTicks = 0
        }
    }

    @EventHandler
    fun onPlayerHit(event: EntityDamageByEntityEvent) {
        val attacker = event.damager
        val entity = event.entity

        if (attacker !is Player || entity !is Player) return
        if (ActiveHearts.getHearts(attacker.uniqueId).contains(Frozen)) {
            if (Math.random() >= 0.1) return // 10% chance
            if (TrustRegistry.getTrusted(attacker.uniqueId).contains(entity.uniqueId)) return
            plugin.logger.info("Frozen Heart hit event: Inflicting Frozen on ${entity.name} by ${attacker.name}")
            entity.freezeTicks = 100 // 5-second freeze - hoping this works
            attacker.sendMessage("§aYou froze ${entity.name}!")
        }
    }

    override fun specialEffect(player: Player) {
        val location = player.location.clone()
        val radius = 10.0 // 10 blocks radius - editable

        location.world.getNearbyEntities(location, radius, radius, radius).forEach { entity ->
            if (entity is Player && entity != player) {
                entity.freezeTicks = 200 // 10-second freeze
                player.sendMessage("§aYou froze ${entity.name} for 10 seconds!")
            }
        }
    }

    override fun clearCooldown(playerId: UUID) {
        cooldowns.remove(playerId)
    }

    override fun getCooldown(playerId: UUID): Long {
        return cooldowns[playerId] ?: return 0L
    }
}