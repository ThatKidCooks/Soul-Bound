package site.thatkid.soulBound.items.hearts

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import site.thatkid.soulBound.hearts.ActiveHearts
import site.thatkid.soulBound.hearts.TrustRegistry
import site.thatkid.soulBound.items.Heart
import java.util.UUID

object Wither : Heart(), Listener {
    private val plugin: JavaPlugin = JavaPlugin.getProvidingPlugin(Wither::class.java)

    private val cooldowns = mutableMapOf<UUID, Long>()
    val cooldownTime = 120 * 1000L // 2 minutes in milliseconds
    val blownUpBy = mutableMapOf<UUID, UUID>()

    override val key: NamespacedKey
        get() = NamespacedKey(plugin, "wither")

    override fun createItem(): ItemStack {
        val item = ItemStack(Material.APPLE)
        val meta = item.itemMeta!!
        meta.setDisplayName("§0Wither Heart")
        meta.lore(listOf(
            Component.text("§7A heart that carries the burden of decay."),
            Component.text("§7It withers the soul, but grants power."),
            Component.text(""),
            Component.text("§f✧ §710% chance §fto inflict Wither I §1on hit"),
            Component.text(""),
            Component.text("§a§lPower — Wither Blast"),
            Component.text("§7Unleashes a blast of withering energy"),
            Component.text("§7that shoots wither §1heads in the direction you are facing."),
            Component.text("§8Cooldown: 30 seconds")
        ))

        meta.persistentDataContainer.set(key, PersistentDataType.BYTE, 1)

        item.itemMeta = meta
        return item
    }

    override fun constantEffect(player: Player) {
        return
    }

    @EventHandler
    fun onPlayerHit(event: EntityDamageByEntityEvent) {
        val attacker = event.damager
        val entity = event.entity

        if (attacker !is Player || entity !is Player) return
        if (ActiveHearts.getHearts(attacker.uniqueId).contains(this)) {
            if (Math.random() >= 0.1) return // 10% chance
            if (TrustRegistry.trustedPlayers[attacker.uniqueId]?.contains(entity.uniqueId) ?: return) return
            entity.addPotionEffect(PotionEffect(PotionEffectType.WITHER, 100, 0))
            attacker.sendMessage("§aYou inflicted Wither I on ${entity.name}!")
        }
    }

    override fun specialEffect(player: Player) {
        if (cooldowns.containsKey(player.uniqueId) && System.currentTimeMillis() < cooldowns[player.uniqueId]!!) {
            player.sendMessage("§cYou must wait before using Wither Blast again.")
            return
        }

        val direction = player.location.direction
        val numHeads = 5 // Number of wither heads to shoot
        for (i in 0 until numHeads) {
            val head = player.world.spawnEntity(player.location, EntityType.WITHER_SKULL)
            head.velocity = direction.multiply(1.5).add(org.bukkit.util.Vector(0.0, 0.5, 0.0))
        }

        cooldowns[player.uniqueId] = System.currentTimeMillis() + cooldownTime
        player.sendMessage("§aWither Blast unleashed!")
    }

    override fun clearCooldown(playerId: UUID) {
        cooldowns.remove(playerId)
    }

    override fun getCooldown(playerId: UUID): Long {
        return cooldowns[playerId] ?: 0L
    }
}