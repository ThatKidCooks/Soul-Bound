package site.thatkid.soulBound.items.hearts.normal

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Sound
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
import site.thatkid.soulBound.HeartRegistry.golemListener
import site.thatkid.soulBound.hearts.TrustRegistry
import site.thatkid.soulBound.items.Heart
import java.util.UUID

object Golem: Heart() {
    private val plugin: JavaPlugin
        get() = JavaPlugin.getProvidingPlugin(Golem::class.java)

    override val key: NamespacedKey = NamespacedKey(plugin, "golem")

    private val cooldowns = mutableMapOf<UUID, Long>()
    private val cooldownTime = 90 * 1000L // 1:30 minutes in milliseconds
    private val knockbackImmune = mutableMapOf<UUID, Long>()

    override fun createItem(): ItemStack {
        val item = ItemStack(Material.APPLE)
        val meta = item.itemMeta!!
        meta.displayName(Component.text("§7Golem Heart"))
        meta.lore(listOf(
            Component.text("§7Forged from iron and determination."),
            Component.text(""),
            Component.text("§f✧ §7Permanent §fResistance I"),
            Component.text(""),
            Component.text("§7§lPower — Iron Might"),
            Component.text("§7Slam the ground, knocking back enemies"),
            Component.text("§7and gaining §cStrength II §7and §fResistance II §7for §f10s"),
            Component.text("§7Also grants §fknockback immunity §7for §f10s"),
            Component.text("§8Cooldown: 90 seconds")
        ))
        meta.persistentDataContainer.set(key, PersistentDataType.BYTE, 1)
        item.itemMeta = meta
        return item
    }


    override fun constantEffect(player: Player) {
        // Permanent Resistance I
            player.addPotionEffect(
                PotionEffect(PotionEffectType.RESISTANCE, 50, 0, false, true)
            )

        // Check if knockback immunity has expired
        val now = System.currentTimeMillis()
        knockbackImmune[player.uniqueId]?.let { immuneUntil ->
            if (now > immuneUntil) {
                knockbackImmune.remove(player.uniqueId)
            }
        }
    }

    override fun specialEffect(player: Player) {
        val now = System.currentTimeMillis()

        if (cooldowns[player.uniqueId]?.let { now - it < cooldownTime } == true) {
            val remaining = (cooldownTime - (now - cooldowns[player.uniqueId]!!)) / 1000
            player.sendMessage(Component.text("§cAbility on cooldown! Wait $remaining seconds."))
            return
        }

        cooldowns[player.uniqueId] = now

        // Grant knockback immunity for 10 seconds
        knockbackImmune[player.uniqueId] = now + (10 * 1000L)

        // Iron Might ability
        player.sendMessage(Component.text("§7You slam the ground with iron might!"))

        // Ground slam effect - knock back nearby players
        val location = player.location
        val world = player.world

        // Particle effects
        world.spawnParticle(Particle.SWEEP_ATTACK, location, 10, 2.0, 0.5, 2.0, 0.0)

        // Sound effect
        world.playSound(location, Sound.ENTITY_IRON_GOLEM_ATTACK, 2.0f, 0.8f)
        world.playSound(location, Sound.BLOCK_ANVIL_LAND, 1.5f, 1.0f)

        val trusted = TrustRegistry.getTrusted(player.uniqueId)

        for (entity in player.world.getNearbyEntities(player.location, 5.0, 5.0, 5.0)) {
            if (entity == player || entity.isDead) continue

            if (entity is Player && trusted.contains(entity.uniqueId)) {
                continue // skip trusted players
            }

            val isHostile = entity is Monster || entity is Slime || entity is Phantom || entity is Ghast
            val isEnemyPlayer = entity is Player

            if ((isHostile || isEnemyPlayer) && entity is LivingEntity) {
                val direction = entity.location.toVector().subtract(player.location.toVector()).normalize().multiply(3)
                direction.y = 2.0
                entity.velocity = direction

            }
        }

        // Apply buffs to the user
        player.addPotionEffect(PotionEffect(PotionEffectType.STRENGTH, 10 * 20, 1, false, true)) // Strength II for 10 seconds
        player.addPotionEffect(PotionEffect(PotionEffectType.RESISTANCE, 10 * 20, 1, false, true)) // Resistance II for 10 seconds
    }

    override fun checkProgress(player: Player): String {
        return golemListener.getProgress(player)
    }


    override fun clearCooldown(playerId: UUID) {
        cooldowns.remove(playerId)
        knockbackImmune.remove(playerId)
    }

    override fun getCooldown(playerId: UUID): Long {
        return cooldowns[playerId] ?: 0L
    }

    // Method to check if player has knockback immunity
    fun hasKnockbackImmunity(playerId: UUID): Boolean {
        val now = System.currentTimeMillis()
        return knockbackImmune[playerId]?.let { it > now } ?: false
    }
}