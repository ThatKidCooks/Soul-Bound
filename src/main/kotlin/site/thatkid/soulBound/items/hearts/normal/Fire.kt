package site.thatkid.soulBound.items.hearts.normal

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Ghast
import org.bukkit.entity.Monster
import org.bukkit.entity.Phantom
import org.bukkit.entity.Player
import org.bukkit.entity.Slime
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import site.thatkid.soulBound.hearts.TrustRegistry
import site.thatkid.soulBound.items.Heart
import java.util.UUID
import kotlin.math.cos
import kotlin.math.sin

object Fire : Heart() {

    private val plugin: JavaPlugin
        get() = JavaPlugin.getProvidingPlugin(Fire::class.java)

    private val cooldowns = mutableMapOf<UUID, Long>()
    private var remaining: Long = 0L
    private var cooldownTime = 100 * 1000L // 20 seconds in milliseconds

    override val key: NamespacedKey
        get() = NamespacedKey(plugin, "fire")

    override fun createItem(): ItemStack {
        val item = ItemStack(Material.APPLE)
        val meta = item.itemMeta!!
        meta.displayName(Component.text("§cFire Heart"))
        meta.lore(listOf(
            Component.text("§7Few can withstand the heart of fire."),
            Component.text("§cThe Nether will not hand it to you — it will burn away the unworthy."),
            Component.text(""),
            Component.text("§f✧ §7Permanent §cFire Resistance §7& §9Strength when on Fire"),
            Component.text(""),
            Component.text("§3§lPower — Lava Surge"),
            Component.text("§cLaunch Enemies into the Air & Set the Ground on Fire"),
            Component.text("§8Cooldown: 100 seconds")
        ))
        meta.persistentDataContainer.set(key, PersistentDataType.BYTE, 1)
        item.itemMeta = meta
        return item
    }

    override fun constantEffect(player: Player) {
        player.addPotionEffect(PotionEffect(PotionEffectType.FIRE_RESISTANCE, 50, 0, false, false, false))
        if (player.isInLava || player.fireTicks > 0) {
            player.addPotionEffect(PotionEffect(PotionEffectType.STRENGTH, 50, 0, false, false, false))
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

        val knockRadius = 6.0
        val ringRadius = 6.0
        val ringPoints = 56 // smooth circle without overkill
        val trusted = TrustRegistry.getTrusted(player.uniqueId)

        // Launch and ping nearby enemies
        for (entity in player.world.getNearbyEntities(player.location, knockRadius, knockRadius, knockRadius)) {
            if (entity == player || entity.isDead) continue
            if (entity is Player && trusted.contains(entity.uniqueId)) continue

            val isHostile = entity is Monster || entity is Slime || entity is Phantom || entity is Ghast
            val isEnemyPlayer = entity is Player

            if (isHostile || isEnemyPlayer) {
                val direction = entity.location.toVector().subtract(player.location.toVector()).normalize()
                direction.y = 4.0
                entity.velocity = direction
                player.world.playSound(entity.location, Sound.ENTITY_GENERIC_HURT, 1f, 1f)
            }
        }

        val durationTicks = 100L // 5 seconds
        spawnVerticalRingParticles(player, ringRadius, ringPoints, durationTicks)
        val placed = igniteGroundRing(player, ringRadius, ringPoints)
        scheduleExtinguish(placed, durationTicks)

        giveAlliesFireRes(player, 16.0, durationTicks.toInt())

        player.sendMessage(Component.text("§aYou used §lLava Surge§r§a!"))
        player.world.spawnParticle(Particle.EXPLOSION, player.location, 6)
        player.world.playSound(player.location, Sound.ENTITY_BLAZE_SHOOT, 1.0f, 0.7f)
    }

//    override fun checkProgress(player: Player): String {
//        val tracker = HeartRegistry.fireTracker
//        val uuid = player.uniqueId
//
//        return if (tracker.isHeartClaimed()) {
//            if (tracker.hasReceived(uuid)) {
//                "§cFire Heart §8| §aUnlocked by you"
//            } else {
//                val winner = tracker.getWinnerName() ?: "another player"
//                "§cFire Heart §8| §cAlready claimed by $winner"
//            }
//        } else {
//            val status = if (tracker.hasKilledWither(uuid)) {
//                "§a✓ Requirement complete — awaiting award"
//            } else {
//                "§7Kill a Wither to qualify"
//            }
//            "§cFire Heart Progress: $status"
//        }
//    }



    override fun clearCooldown(playerId: UUID) {
        cooldowns.remove(playerId)
    }

    override fun getCooldown(playerId: UUID): Long {
        return cooldowns[playerId] ?: 0L
    }

    private fun circlePoints(center: Location, radius: Double, points: Int): List<Location> {
        val list = ArrayList<Location>(points)
        val world = center.world
        val cx = center.x
        val cz = center.z
        for (i in 0 until points) {
            val angle = 2.0 * Math.PI * i / points
            val x = cx + radius * cos(angle)
            val z = cz + radius * sin(angle)
            list.add(Location(world, x, center.y, z))
        }
        return list
    }

    private fun spawnVerticalRingParticles(player: Player, radius: Double, points: Int, durationTicks: Long) {
        val world = player.world
        val ring = circlePoints(player.location, radius, points)
        val maxY = world.maxHeight - 1
        val minY = player.location.y.toInt().coerceAtLeast(world.minHeight)
        var tick = 0

        val task = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            if (!player.isOnline) return@Runnable
            val phase = tick % 6

            for (loc in ring) {
                val bx = loc.x
                val bz = loc.z
                var y = minY + phase
                while (y <= maxY) {
                    world.spawnParticle(Particle.FLAME, bx, y.toDouble(), bz, 2, 0.15, 0.3, 0.15, 0.001)
                    world.spawnParticle(Particle.LARGE_SMOKE, bx, y.toDouble(), bz, 1, 0.1, 0.2, 0.1, 0.0)
                    y += 6
                }
            }

            tick++
        }, 0L, 2L)

        Bukkit.getScheduler().runTaskLater(plugin, Runnable { task.cancel() }, durationTicks)
    }

    private fun igniteGroundRing(player: Player, radius: Double, points: Int): MutableSet<Block> {
        val world = player.world
        val placed = LinkedHashSet<Block>()
        val ring = circlePoints(player.location, radius, points)

        for (loc in ring) {
            val bx = loc.blockX
            val bz = loc.blockZ
            val base = world.getHighestBlockAt(bx, bz)
            val above = base.getRelative(BlockFace.UP)

            if (above.type == Material.AIR) {
                above.type = Material.FIRE
                placed.add(above)
            }
        }
        return placed
    }

    private fun giveAlliesFireRes(player: Player, radius: Double, durationTicks: Int) {
        val trusted = TrustRegistry.getTrusted(player.uniqueId)
        for (entity in player.world.getNearbyEntities(player.location, radius, radius, radius)) {
            if (entity is Player && entity.uniqueId != player.uniqueId && trusted.contains(entity.uniqueId)) {
                entity.addPotionEffect(PotionEffect(PotionEffectType.FIRE_RESISTANCE, durationTicks, 0, false, false, true))
                entity.sendMessage(Component.text("§6You are protected by §lLava Surge§r§6!"))
            }
        }
    }

    private fun scheduleExtinguish(blocks: Set<Block>, delayTicks: Long) {
        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            for (b in blocks) if (b.type == Material.FIRE) b.type = Material.AIR
        }, delayTicks)
    }

}