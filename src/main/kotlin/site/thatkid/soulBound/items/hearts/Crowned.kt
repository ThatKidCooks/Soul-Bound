package site.thatkid.soulBound.items.hearts

import net.kyori.adventure.text.Component
import org.bukkit.*
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
import site.thatkid.soulBound.HeartRegistry
import site.thatkid.soulBound.hearts.TrustRegistry
import site.thatkid.soulBound.items.Heart
import java.util.*

object Crowned : Heart() {

    private var remaining: Long = 0L
    private val plugin: JavaPlugin
        get() = JavaPlugin.getProvidingPlugin(Crowned::class.java)

    override val key: NamespacedKey
        get() = NamespacedKey(plugin, "crowned")
    private val cooldowns = mutableMapOf<UUID, Long>()
    private var cooldownTime = 40 * 1000L // 40 seconds in milliseconds

    val smashedBy = mutableMapOf<UUID, UUID>()

    override fun createItem(): ItemStack {
        val item = ItemStack(Material.APPLE)
        val meta = item.itemMeta!!

        meta.displayName(Component.text("§aCrowned Heart"))
        meta.lore(listOf(
            Component.text("§7A heart split between two souls."),
            Component.text("§7Fueled by conflict and loyalty."),
            Component.text(""),
            Component.text("§f✧ §7Permanent §fSpeed I"),
            Component.text(""),
            Component.text("§a§lPower — Smash"),
            Component.text("§7Damages and blasts away enemies within §e6 blocks"),
            Component.text("§7Deals §c3 hearts§7 to mobs and players."),
            Component.text("§8Cooldown: 20 seconds")
        ))

        meta.persistentDataContainer.set(key, PersistentDataType.BYTE, 1)
        item.itemMeta = meta
        return item
    }


    override fun constantEffect(player: Player) {
        // Apply Speed I if not already applied
        if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
            player.addPotionEffect(
                PotionEffect(PotionEffectType.SPEED, 40, 0, false, true)
            )
        }
    }

    override fun specialEffect(player: Player) {
        val now = System.currentTimeMillis()

        if (cooldowns[player.uniqueId]?.let { now - it < cooldownTime } == true) {
            remaining = (cooldownTime - (now - cooldowns[player.uniqueId]!!)) / 1000
            player.sendMessage(Component.text("§cAbility on cooldown! Wait $remaining seconds."))
            return
        }

        cooldowns[player.uniqueId] = now

        val radius = 6.0
        val damage = 6.0 // 3 hearts
        val trusted = TrustRegistry.getTrusted(player.uniqueId)

        for (entity in player.world.getNearbyEntities(player.location, radius, radius, radius)) {
            if (entity == player || entity.isDead) continue

            if (entity is Player && trusted.contains(entity.uniqueId)) {
                continue // skip trusted players
            }

            val isHostile = entity is Monster || entity is Slime || entity is Phantom || entity is Ghast
            val isEnemyPlayer = entity is Player

            if ((isHostile || isEnemyPlayer) && entity is LivingEntity) {
                if (entity is Player) {
                    smashedBy[entity.uniqueId] = player.uniqueId
                }
                val direction = entity.location.toVector().subtract(player.location.toVector()).normalize().multiply(3)
                direction.y = 0.6
                entity.velocity = direction

                val finalHealth = (entity.health - damage).coerceAtLeast(0.0)
                entity.health = finalHealth

                player.world.playSound(entity.location, Sound.ENTITY_GENERIC_HURT, 1f, 1f)

            }
        }

        player.sendMessage(Component.text("§aYou used §lSmash§r§a!"))
        player.world.playSound(player.location, Sound.ENTITY_WITHER_BREAK_BLOCK, 1f, 0.5f)
        player.world.spawnParticle(Particle.EXPLOSION, player.location, 6)
    }

//    override fun checkProgress(player: Player): String {
//        val tracker = HeartRegistry.crownedTracker
//        val uuid = player.uniqueId
//
//        val kills = tracker.getKills(uuid)
//        val required = tracker.getKillsRequired()
//        val percent = ((kills.toDouble() / required) * 100).toInt().coerceIn(0, 100)
//
//        return when {
//            tracker.isClaimed() && tracker.isOwner(uuid) ->
//                "§eCrowned Heart §8| §aUnlocked by you"
//
//            tracker.isClaimed() && !tracker.isOwner(uuid) -> {
//                val winner = tracker.getOwnerName() ?: "another player"
//                "§eCrowned Heart §8| §cAlready claimed by $winner"
//            }
//
//            kills >= required ->
//                "§eCrowned Heart §8| §a✓ Requirement complete — awaiting award"
//
//            else ->
//                "§eCrowned Heart Progress: §b$kills§7/§b$required kills §8($percent%)"
//        }
//    }



    override fun clearCooldown(uuid: UUID) {
        cooldowns.remove(uuid)
    }

    override fun getCooldown(playerId: UUID): Long {
        return cooldowns[playerId] ?: 0L
    }
}
