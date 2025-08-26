package site.thatkid.soulBound.items.hearts.normal

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import site.thatkid.soulBound.HeartRegistry.speedListener
import site.thatkid.soulBound.items.Heart
import site.thatkid.soulBound.items.ItemCreator
import java.util.UUID

object Speed : Heart() {

    private val plugin: JavaPlugin
        get() = JavaPlugin.getProvidingPlugin(Speed::class.java) // Get the plugin instance

    private val cooldowns = mutableMapOf<UUID, Long>() // Cooldown map to track when players used their special ability
    private var cooldownTime = 100 * 1000L // 30 seconds in milliseconds
    private var remaining: Long = 0L // Remaining cooldown time

    override val key: NamespacedKey
        get() = NamespacedKey(plugin, "speed") // Overrides the key in Heart

    override fun createItem(): ItemStack {
        return ItemCreator.itemCreator(8)
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

        player.velocity = player.location.direction.multiply(3) // Dash forward

        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 20 * 10, 3, false, false, false)) // Add speed IV
        player.addPotionEffect(PotionEffect(PotionEffectType.JUMP_BOOST, 20 * 10, 1, false, false, false)) // Add jump boost II
    }

    override fun checkProgress(player: Player): String {
        return speedListener.getProgress(player)
    }

    override fun clearCooldown(playerId: UUID) {
        cooldowns.remove(playerId) // Remove the cooldown for the player
    }

    override fun getCooldown(playerId: UUID): Long {
        val lastUsed = cooldowns[playerId] ?: return 0L
        val now = System.currentTimeMillis()
        val remaining = cooldownTime - (now - lastUsed)
        return if (remaining > 0) remaining else 0L
    }
}