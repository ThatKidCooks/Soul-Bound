package site.thatkid.soulBound.items.hearts

import com.comphenix.protocol.wrappers.EnumWrappers
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
import site.thatkid.soulBound.HeartRegistry
import site.thatkid.soulBound.items.Heart
import java.util.UUID

/**
 * The Aquatic Heart - obtained by swimming 5000 blocks in water.
 * 
 * **Passive Abilities:**
 * - Permanent Dolphin's Grace (faster swimming)
 * - Permanent Conduit Power II (underwater vision and breathing)
 * 
 * **Special Ability - Tidal Surge:**
 * - If used while in water, grants Strength III for 5 seconds
 * - Cooldown: 100 seconds
 * - Creates bubble particle effects when activated
 * 
 * This heart is perfect for players who spend a lot of time underwater or near oceans.
 * The constant swimming buffs make underwater exploration much more efficient.
 */
object Aquatic: Heart() {
    /** Plugin instance for creating namespaced keys */
    private val plugin: JavaPlugin = JavaPlugin.getProvidingPlugin(Aquatic::class.java)

    /** Unique identifier for this heart type */
    override val key: NamespacedKey = NamespacedKey(plugin, "aquatic")

    /** Tracks cooldowns for each player's special ability usage */
    private val cooldowns = mutableMapOf<UUID, Long>()
    
    /** Cooldown time for the Tidal Surge ability (100 seconds) */
    private val cooldownTime = 100 * 1000L // 100 seconds in milliseconds

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

    /**
     * Activates the Tidal Surge special ability.
     * 
     * When used in water, grants Strength III for 5 seconds and creates dramatic
     * bubble particle effects. If not in water, only the particles and cooldown occur.
     * 
     * The ability has a 100-second cooldown to prevent spam usage.
     * 
     * @param player The player attempting to use Tidal Surge
     */
    override fun specialEffect(player: Player) {
        val now = System.currentTimeMillis()

        // Check if ability is on cooldown
        if (cooldowns[player.uniqueId]?.let { now - it < cooldownTime } == true) {
            val remaining = (cooldownTime - (now - cooldowns[player.uniqueId]!!)) / 1000
            player.sendMessage(Component.text("§cAbility on cooldown! Wait $remaining seconds."))
            return
        }

        // Create dramatic bubble particle effect
        player.spawnParticle(Particle.BUBBLE, player.location, 1000)

        // Set cooldown and notify player
        cooldowns[player.uniqueId] = now
        player.sendMessage(Component.text("§bYou feel a surge of aquatic energy!"))

        // Grant Strength III for 5 seconds if player is in water
        if (player.isInWater) {
            player.addPotionEffect(PotionEffect(PotionEffectType.STRENGTH, 5 * 20, 2, false, false))
        }
    }

    /**
     * Displays the player's progress toward obtaining the Aquatic Heart.
     * 
     * Shows different messages based on the current state:
     * - If already obtained by this player
     * - If obtained by another player (since only one exists globally)
     * - If requirements are met but not yet awarded
     * - Current progress with percentage completion
     * 
     * Requirements: Swim 5000 blocks in water
     * 
     * @param player The player to check progress for
     * @return Formatted progress message with color codes
     */
    override fun checkProgress(player: Player): String {
        val tracker = HeartRegistry.aquaticTracker
        val uuid = player.uniqueId

        // Calculate distance and progress percentage
        val dist = tracker.getDistance(uuid).toInt()
        val required = tracker.getDistanceRequired()
        val percent = (dist.toDouble() / required * 100).toInt().coerceAtMost(100)

        return when {
            // Player has already unlocked it
            tracker.hasReceived(uuid) ->
                "§3Aquatic Heart §8| §aUnlocked by you"

            // Someone else got it (single-award globally)
            tracker.isGloballyReceived() -> {
                val winner = tracker.getGlobalWinnerName() ?: "another player"
                "§3Aquatic Heart §8| §cAlready claimed by $winner"
            }

            // Requirement complete, just needs claiming
            dist >= required ->
                "§3Aquatic Heart §8| §a✓ Requirement complete — awaiting award"

            // Still progressing
            else ->
                "§3Aquatic Heart Progress: §b$dist§7/§b$required blocks §8($percent%)"
        }
    }

    /**
     * Clears the cooldown for this player's Tidal Surge ability.
     * 
     * @param playerId UUID of the player whose cooldown should be cleared
     */
    override fun clearCooldown(playerId: UUID) {
        cooldowns.remove(playerId)
    }

    /**
     * Gets the remaining cooldown time for this player's Tidal Surge ability.
     * 
     * @param playerId UUID of the player to check
     * @return Remaining cooldown time in milliseconds, or 0 if no cooldown
     */
    override fun getCooldown(playerId: UUID): Long {
        return cooldowns[playerId] ?: 0L
    }
}