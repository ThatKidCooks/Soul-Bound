package site.thatkid.soulBound.items.hearts

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import site.thatkid.soulBound.items.Heart
import java.util.UUID

object Speed : Heart() {

    private val plugin: JavaPlugin = JavaPlugin.getProvidingPlugin(Speed::class.java) // Get the plugin instance

    private val cooldowns = mutableMapOf<UUID, Long>() // Cooldown map to track when players used their special ability
    private var cooldownTime = 30 * 1000L // 30 seconds in milliseconds
    private var remaining: Long = 0L // Remaining cooldown time

    override val key: NamespacedKey
        get() = NamespacedKey(plugin, "speed") // Overrides the key in Heart

    override fun createItem(): ItemStack {
        val item = ItemStack(Material.APPLE) // Apple
        val meta = item.itemMeta!! // Set meta do be the items meta
        meta.displayName(Component.text("§eSpeed Heart")) // Display name
        meta.lore(listOf(
            Component.text("§7Born from the wind. Runs endlessly."),
            Component.text(""),
            Component.text("§f✧ §7Permanent §bSpeed II"),
            Component.text(""),
            Component.text("§a§lPower — Lightning Dash"),
            Component.text("§7Dash forward and gain"),
            Component.text("§7§fSpeed IV §7and §eJump Boost II §7for §f10s"),
            Component.text("§8Cooldown: 60 seconds")
        )) // Lore
        meta.persistentDataContainer.set(key, PersistentDataType.BYTE, 1)
        item.itemMeta = meta
        return item
    }

    override fun constantEffect(player: Player) {
        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 50, 1, false, false, false)) // Speed II
    }

    override fun specialEffect(player: Player) {

        val now = System.currentTimeMillis()

        if (cooldowns[player.uniqueId]?.let { now - it < cooldownTime } == true) {
            remaining = (cooldownTime - (now - cooldowns[player.uniqueId]!!)) / 1000
            player.sendMessage(Component.text("§cAbility on cooldown! Wait $remaining seconds."))
            return
        }

        cooldowns[player.uniqueId] = now

        player.velocity = player.location.direction.multiply(2) // Dash forward

        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 20 * 10, 3, false, false, false)) // Add speed IV
        player.addPotionEffect(PotionEffect(PotionEffectType.JUMP_BOOST, 20 * 10, 2, false, false, false)) // Add jump boost II
    }

    override fun clearCooldown(playerId: UUID) {
        cooldowns.remove(playerId) // Remove the cooldown for the player
    }

    override fun getCooldown(playerId: UUID): Long {
        return cooldowns[playerId] ?: 0L // Return 0 if no cooldown exists
    }
}