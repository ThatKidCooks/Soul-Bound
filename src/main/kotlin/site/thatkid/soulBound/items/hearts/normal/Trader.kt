package site.thatkid.soulBound.items.hearts.normal

import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import site.thatkid.soulBound.HeartRegistry.speedListener
import site.thatkid.soulBound.HeartRegistry.traderListener
import site.thatkid.soulBound.items.Heart
import site.thatkid.soulBound.items.ItemCreator
import java.util.*

object Trader : Heart() {

    private val plugin: JavaPlugin
        get() = JavaPlugin.getProvidingPlugin(Trader::class.java)
    override val key
        get() = NamespacedKey(plugin, "trader")

    private val cooldowns = mutableMapOf<UUID, Long>()
    private const val COOLDOWN = 150 * 1000L
    private const val DURATION = 100 * 20L  // 100 seconds in ticks

    override fun createItem(): ItemStack {
        return ItemCreator.itemCreator(10)
    }


    override fun constantEffect(player: Player) {
        val existing = player.getPotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE)
        if (existing == null || existing.amplifier < 0 || existing.duration < 60) {
            player.addPotionEffect(
                PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 200, 0, false, false)
            )
        }
    }

    override fun specialEffect(player: Player) {
        val now = System.currentTimeMillis()
        val lastUsed = cooldowns[player.uniqueId] ?: 0L

        if (now - lastUsed < COOLDOWN) {
            val remaining = (COOLDOWN - (now - lastUsed)) / 1000
            player.sendMessage(Component.text("§cTrader boost on cooldown! Wait $remaining seconds."))
            return
        }

        cooldowns[player.uniqueId] = now

        player.addPotionEffect(
            PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, DURATION.toInt(), 254, false, true)
        )

        player.sendMessage(Component.text("§aYou feel revered by villagers everywhere!"))
        player.world.playSound(player.location, Sound.ENTITY_VILLAGER_CELEBRATE, 1f, 1f)
        player.spawnParticle(Particle.HAPPY_VILLAGER, player.location, 40, 1.5, 1.5, 1.5, 0.1)
    }

    override fun checkProgress(player: Player): String {
        return traderListener.getProgress(player.uniqueId)
    }


    override fun clearCooldown(uuid: UUID) {
        cooldowns.remove(uuid)
    }

    override fun getCooldown(playerId: UUID): Long {
        val lastUsed = cooldowns[playerId] ?: return 0L
        val now = System.currentTimeMillis()
        val remaining = COOLDOWN - (now - lastUsed)
        return if (remaining > 0) remaining else 0L
    }
}
