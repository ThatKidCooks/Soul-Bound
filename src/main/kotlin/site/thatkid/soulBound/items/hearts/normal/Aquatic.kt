package site.thatkid.soulBound.items.hearts.normal

import net.axay.kspigot.extensions.bukkit.isFeetInWater
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import site.thatkid.soulBound.items.Heart
import java.util.UUID

object Aquatic: Heart() {
    private val plugin: JavaPlugin = JavaPlugin.getProvidingPlugin(Aquatic::class.java) // Get the plugin instance

    override val key: NamespacedKey
        get() = NamespacedKey(plugin, "aquatic")

    private val cooldowns = mutableMapOf<UUID, Long>()
    private val cooldownTime = 100 * 1000L // 20 seconds in milliseconds

    override fun createItem(): ItemStack {
        val item = ItemStack(Material.APPLE)
        val meta = item.itemMeta!!
        meta.displayName(Component.text("§bAquatic Heart"))
        meta.lore(listOf(
            Component.text("§7Born from the ocean. Swam away."),
            Component.text(""),
            Component.text("§f✧ §7Permanent §bDolphin’s Grace §7& §9Conduit Power"),
            Component.text(""),
            Component.text("§3§lPower — Tidal Surge"),
            Component.text("§7If in water, gain §cStrength III §7for §f5s"),
            Component.text("§8Cooldown: 100 seconds")
        ))
        meta.persistentDataContainer.set(key, PersistentDataType.BYTE, 1)
        item.itemMeta = meta
        return item
    }

    override fun constantEffect(player: Player) {
        if (!player.hasPotionEffect(PotionEffectType.DOLPHINS_GRACE)) {
            player.addPotionEffect(
                PotionEffect(PotionEffectType.DOLPHINS_GRACE, 40, 0, false, false)
            )
        }
        if (!player.hasPotionEffect(PotionEffectType.CONDUIT_POWER)) {
            player.addPotionEffect(
                PotionEffect(PotionEffectType.CONDUIT_POWER, 40, 1, false, false)
            )
        }
    }

    override fun specialEffect(player: Player) {
        val now = System.currentTimeMillis()

        if (cooldowns[player.uniqueId]?.let { now - it < cooldownTime } == true) {
            val remaining = (cooldownTime - (now - cooldowns[player.uniqueId]!!)) / 1000
            player.sendMessage(Component.text("§cAbility on cooldown! Wait $remaining seconds."))
            return
        }

        // particles
        player.spawnParticle(Particle.BUBBLE, player.location, 1000)

        cooldowns[player.uniqueId] = now
        player.sendMessage(Component.text("§bYou feel a surge of aquatic energy!"))

        if (player.isInWaterOrRain || player.isInWater || player.isFeetInWater) {
            player.addPotionEffect(PotionEffect(PotionEffectType.STRENGTH, 10 * 20, 2, false, false))
        }
    }

//    override fun checkProgress(player: Player): String {
//        val tracker = HeartRegistry.aquaticTracker
//        val uuid = player.uniqueId
//
//        // Calculate distance and progress percentage
//        val dist = tracker.getDistance(uuid).toInt()
//        val required = tracker.getDistanceRequired()
//        val percent = (dist.toDouble() / required * 100).toInt().coerceAtMost(100)
//
//        return when {
//            tracker.hasReceived(uuid) ->
//                "§3Aquatic Heart §8| §aUnlocked by you"
//
//            tracker.isGloballyReceived() -> {
//                val winner = tracker.getGlobalWinnerName() ?: "another player"
//                "§3Aquatic Heart §8| §cAlready claimed by $winner"
//            }
//
//            dist >= required ->
//                "§3Aquatic Heart §8| §a✓ Requirement complete — awaiting award"
//
//            else ->
//                "§3Aquatic Heart Progress: §b$dist§7/§b$required blocks §8($percent%)"
//        }
//    }



    override fun clearCooldown(playerId: UUID) {
        cooldowns.remove(playerId)
    }

    override fun getCooldown(playerId: UUID): Long {
        return cooldowns[playerId] ?: 0L
    }
}