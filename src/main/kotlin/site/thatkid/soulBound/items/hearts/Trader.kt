package site.thatkid.soulBound.items.hearts

import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import site.thatkid.soulBound.HeartRegistry
import site.thatkid.soulBound.items.Heart
import java.util.*

object Trader : Heart() {

    private val plugin = JavaPlugin.getProvidingPlugin(Trader::class.java)
    override val key = NamespacedKey(plugin, "trader")

    private val cooldowns = mutableMapOf<UUID, Long>()
    private const val COOLDOWN = 150 * 1000L
    private const val DURATION = 100 * 20L  // 100 seconds in ticks

    override fun createItem(): ItemStack {
        val item = ItemStack(Material.APPLE)
        val meta = item.itemMeta!!
        meta.displayName(Component.text("§2Trader Heart"))
        meta.lore(listOf(
            Component.text("§7Unlock by earning the"),
            Component.text("§fHero of the Village §7advancement."),
            Component.text(""),
            Component.text("§f✧ §7Permanent §aHero of the Village I"),
            Component.text(""),
            Component.text("§2§lPower — Royal Bargain"),
            Component.text("§7Grants §aHero of the Village 255 §7for §f100s"),
            Component.text("§8Cooldown: 2m 30s")
        ))
        meta.persistentDataContainer.set(key, PersistentDataType.BYTE, 1)
        item.itemMeta = meta
        return item
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
        val tracker = HeartRegistry.traderTracker
        val uuid = player.uniqueId

        if (tracker.isHeartClaimed()) {
            return if (tracker.hasReceived(uuid)) {
                "§2Trader Heart §8| §aUnlocked by you"
            } else {
                val winner = tracker.getWinnerName() ?: "another player"
                "§2Trader Heart §8| §cAlready claimed by $winner"
            }
        }

        val progress = tracker.getProgress(uuid)
        val total = tracker.getTotalRequired()

        val percent = ((progress.toDouble() / total) * 100).toInt().coerceAtMost(100)

        return "§2Trader Heart Progress: §e$progress§7/§e$total professions §8($percent%)"
    }


    override fun clearCooldown(uuid: UUID) {
        cooldowns.remove(uuid)
    }

    override fun getCooldown(playerId: UUID): Long {
        return cooldowns[playerId] ?: 0L
    }
}
