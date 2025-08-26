package site.thatkid.soulBound.items.hearts.normal

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
import site.thatkid.soulBound.HeartRegistry.frozenListener
import site.thatkid.soulBound.hearts.ActiveHearts
import site.thatkid.soulBound.hearts.TrustRegistry
import site.thatkid.soulBound.items.Heart
import site.thatkid.soulBound.items.ItemCreator
import java.util.UUID

object Frozen : Heart(), Listener {
    private val plugin: JavaPlugin
        get() = JavaPlugin.getProvidingPlugin(Frozen::class.java)

    private val cooldowns = mutableMapOf<UUID, Long>()
    private var remaining: Long = 0L
    private var cooldownTime = 100 * 1000L // 20 seconds in milliseconds

    override val key: NamespacedKey
        get() = NamespacedKey(plugin, "frozen")

    override fun createItem(): ItemStack {
        return ItemCreator.itemCreator(4)
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
            entity.freezeTicks = 1000 // 5-second freeze - hoping this works
            entity.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 200, 1, true, false, false))
            attacker.sendMessage("§aYou froze ${entity.name}!")
        }
    }

    override fun specialEffect(player: Player) {

        val now = System.currentTimeMillis()

        if (cooldowns[player.uniqueId]?.let { now - it < cooldownTime } == true) {
            val remaining = (cooldownTime - (now - cooldowns[player.uniqueId]!!)) / 1000
            player.sendMessage(Component.text("§cAbility on cooldown! Wait $remaining seconds."))
            return
        }

        val location = player.location.clone()
        val radius = 10.0 // 10 blocks radius - editable

        location.world.getNearbyEntities(location, radius, radius, radius).forEach { entity ->
            if (entity is Player && entity != player) {
                if (TrustRegistry.getTrusted(player.uniqueId).contains(entity.uniqueId)) return@forEach
                entity.freezeTicks = 200 // 10-second freeze
                entity.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 200, 1, true, false, false))
                player.sendMessage("§aYou froze ${entity.name} for 10 seconds!")
            }
        }
    }

    override fun checkProgress(player: Player): String {
        return frozenListener.getProgress(player.uniqueId)
    }

    override fun clearCooldown(playerId: UUID) {
        cooldowns.remove(playerId)
    }

    override fun getCooldown(playerId: UUID): Long {
        val lastUsed = cooldowns[playerId] ?: return 0L
        val now = System.currentTimeMillis()
        val remaining = cooldownTime - (now - lastUsed)
        return if (remaining > 0) remaining else 0L
    }
}