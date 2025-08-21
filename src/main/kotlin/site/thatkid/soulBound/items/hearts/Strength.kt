package site.thatkid.soulBound.items.hearts

import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import site.thatkid.soulBound.HeartRegistry
import site.thatkid.soulBound.items.Heart
import java.util.*

/**
 * The Strength Heart - obtained by killing 10 players in PvP.
 * 
 * **Passive Abilities:**
 * - Permanent Strength I (increased melee damage)
 * 
 * **Special Ability - Unstoppable Force:**
 * - Grants Strength II and Speed II for 15 seconds
 * - Creates dramatic totem particle effects
 * - Cooldown: 60 seconds
 * 
 * This is an advanced PvP heart that requires more kills than the Crowned Heart.
 * The permanent strength boost makes the holder more dangerous in combat, while
 * the special ability can turn the tide of battle with its powerful temporary buffs.
 * 
 * Requirements: 10 player kills
 */
object Strength : Heart() {

    /** Plugin instance for creating namespaced keys */
    private val plugin = JavaPlugin.getProvidingPlugin(Strength::class.java)
    
    /** Unique identifier for this heart type */
    override val key = NamespacedKey(plugin, "strength")

    /** Tracks cooldowns for each player's Unstoppable Force ability */
    private val cooldowns = mutableMapOf<UUID, Long>()
    
    /** Cooldown time for Unstoppable Force (60 seconds) */
    private const val COOLDOWN = 60_000L
    
    /** Duration of the Unstoppable Force buff (15 seconds in ticks) */
    private const val DURATION = 15 * 20L // 15 seconds in ticks

    /**
     * Creates the Strength Heart item with appropriate display properties.
     * 
     * Shows the PvP requirement and clearly lists both the passive strength
     * boost and the powerful Unstoppable Force ability.
     * 
     * @return ItemStack representing the Strength Heart
     */
    override fun createItem(): ItemStack {
        val item = ItemStack(Material.APPLE)
        val meta = item.itemMeta!!
        meta.displayName(Component.text("§4Strength Heart"))
        meta.lore(listOf(
            Component.text("§7Kill 10 players to earn this."),
            Component.text(""),
            Component.text("§f✧ §7Permanent §cStrength I"),
            Component.text(""),
            Component.text("§4§lPower — Unstoppable Force"),
            Component.text("§7Gain §cStrength II §7and §bSpeed II §7for §f15s"),
            Component.text("§8Cooldown: 60 seconds")
        ))
        meta.persistentDataContainer.set(key, PersistentDataType.BYTE, 1)
        item.itemMeta = meta
        return item
    }


    override fun constantEffect(player: Player) {
        val current = player.getPotionEffect(PotionEffectType.STRENGTH)
        if (current == null || current.amplifier < 0 || current.duration < 60) {
            player.addPotionEffect(
                PotionEffect(PotionEffectType.STRENGTH, 200, 0, false, false)
            )
        }
    }

    override fun specialEffect(player: Player) {
        val now = System.currentTimeMillis()
        val last = cooldowns[player.uniqueId] ?: 0
        if (now - last < COOLDOWN) {
            val left = (COOLDOWN - (now - last)) / 1000
            player.sendMessage(Component.text("§cStrength boost is on cooldown! Wait $left seconds."))
            return
        }

        cooldowns[player.uniqueId] = now

        player.addPotionEffect(PotionEffect(PotionEffectType.STRENGTH, DURATION.toInt(), 1, false, true))
        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, DURATION.toInt(), 1, false, true))
        player.sendMessage(Component.text("§cYou feel unstoppable!"))
        player.world.playSound(player.location, Sound.ENTITY_WITHER_SPAWN, 1f, 1f)
        player.spawnParticle(Particle.TOTEM_OF_UNDYING, player.location, 40, 1.5, 1.5, 1.5, 0.1)
    }

    override fun checkProgress(player: Player): String {
        val tracker = HeartRegistry.strengthTracker
        val uuid = player.uniqueId

        if (tracker.isGloballyReceived()) {
            return if (tracker.hasReceived(uuid)) {
                "§4Strength Heart §8| §aUnlocked by you"
            } else {
                "§4Strength Heart §8| §cAlready claimed by another player"
            }
        }

        val kills = tracker.getKills(uuid)
        val required = tracker.getRequired()
        val percent = (kills.toDouble() / required * 100).coerceAtMost(100.0)

        return "§4Strength Heart Progress: §e$kills§7/§e$required PvP kills §8($percent%)"
    }


    override fun clearCooldown(playerId: UUID) {
        cooldowns.remove(playerId)
    }

    override fun getCooldown(playerId: UUID): Long {
        return cooldowns[playerId] ?: 0L
    }
}
