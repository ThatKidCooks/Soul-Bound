package site.thatkid.soulBound.gui.player

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import site.thatkid.soulBound.hearts.ActiveHearts
import site.thatkid.soulBound.items.Heart
import java.util.*

class DisplayHearts(private val plugin: JavaPlugin) : BukkitRunnable() {

    private val ownerStands = mutableMapOf<UUID, ArmorStand>()
    private val visibleViewers = mutableMapOf<UUID, MutableSet<UUID>>()
    private val lastHeartText = mutableMapOf<UUID, String>()

    override fun run() {
        for (owner in Bukkit.getOnlinePlayers()) {
            val text = buildHeartText(owner)

            if (text.isNotEmpty()) {
                owner.sendActionBar(Component.text(text))
            }

            if (isPlayerInvisible(owner)) {
                ensureStandRemoved(owner)
                continue
            }

            val stand = ensureStand(owner, text)

            val loc = owner.location.clone().add(0.0, 2.3, 0.0)
            if (stand.location.distanceSquared(loc) > 0.0001) {
                stand.teleport(loc)
            }
            updateViewerVisibility(owner, stand)
        }
        cleanupMissingOwners()
    }

    private fun buildHeartText(player: Player): String {
        val hearts = ActiveHearts.getHearts(player.uniqueId)
        var detailedSymbols = mutableListOf<String>()
        for (heart in hearts) {
            detailedSymbols = detailedSymbols(heart, player)
        }
        return if (detailedSymbols.isNotEmpty()) detailedSymbols.joinToString(" §7| ") else ""
    }

    private fun detailedSymbols(heart: Heart, player: Player): MutableList<String> {
        val list = mutableListOf<String>()

        list.add(heart.createItem().itemMeta.displayName()?.let { LegacyComponentSerializer.legacySection().serialize(it) } ?: "")
        list.add(formatCooldown(heart, player))

        return list
    }

    private fun formatCooldown(heart: Heart, player: Player): String {
        val playerId = player.uniqueId
        val cooldownStart = heart.getCooldown(playerId)
        val cooldownDuration = try {
            val field = heart.javaClass.getDeclaredField("cooldownTime")
            field.isAccessible = true
            field.getLong(heart)
        } catch (e: Exception) {
            0L
        }
        if (cooldownStart == 0L || cooldownDuration == 0L) return "Ready"
        val now = System.currentTimeMillis()
        val remaining = (cooldownDuration - (now - cooldownStart)) / 1000
        return if (remaining > 0) "${remaining}s" else "Ready"
    }

    private fun isPlayerInvisible(player: Player): Boolean {
        return player.isInvisible
    }

    private fun ensureStand(owner: Player, text: String): ArmorStand {
        val existing = ownerStands[owner.uniqueId]
        if (existing != null && !existing.isDead) {
            if (lastHeartText[owner.uniqueId] != text) {
                existing.customName(Component.text(text))
                existing.isCustomNameVisible = text.isNotEmpty()
                lastHeartText[owner.uniqueId] = text
            }
            return existing
        }

        val loc = owner.location.clone().add(0.0, 2.3, 0.0)
        val stand = owner.world.spawnEntity(loc, EntityType.ARMOR_STAND) as ArmorStand
        stand.isVisible = false
        stand.isSmall = true
        stand.setGravity(false)
        stand.isMarker = true
        stand.customName(Component.text(text))
        stand.isCustomNameVisible = text.isNotEmpty()
        stand.canPickupItems = false
        stand.setBasePlate(false)
        stand.setArms(false)

        ownerStands[owner.uniqueId] = stand
        lastHeartText[owner.uniqueId] = text

        visibleViewers.putIfAbsent(owner.uniqueId, mutableSetOf())

        updateViewerVisibility(owner, stand)

        return stand
    }

    private fun updateViewerVisibility(owner: Player, stand: ArmorStand) {
        val viewers = Bukkit.getOnlinePlayers()
        val seenSet = visibleViewers.getOrPut(owner.uniqueId) { mutableSetOf() }

        for (viewer in viewers) {
            if (viewer.uniqueId == owner.uniqueId) {
                if (seenSet.remove(viewer.uniqueId)) {
                    viewer.hideEntity(plugin, stand)
                } else {
                    viewer.hideEntity(plugin, stand)
                }
                continue
            }

            if (viewer.world != owner.world || viewer.location.distanceSquared(owner.location) > 60.0 * 60.0) {
                if (seenSet.remove(viewer.uniqueId)) {
                    viewer.hideEntity(plugin, stand)
                }
                continue
            }

            val hasLoS = viewer.hasLineOfSight(stand)

            if (hasLoS) {
                if (seenSet.add(viewer.uniqueId)) {
                    viewer.showEntity(plugin, stand)
                }
            } else {
                if (seenSet.remove(viewer.uniqueId)) {
                    viewer.hideEntity(plugin, stand)
                }
            }
        }
    }

    private fun ensureStandRemoved(owner: Player) {
        val stand = ownerStands.remove(owner.uniqueId)
        if (stand != null && !stand.isDead) {
            Bukkit.getOnlinePlayers().forEach { it.hideEntity(plugin, stand) }
            stand.remove()
        }
        visibleViewers.remove(owner.uniqueId)
        lastHeartText.remove(owner.uniqueId)
    }

    private fun cleanupMissingOwners() {
        val online = Bukkit.getOnlinePlayers().map { it.uniqueId }.toSet()
        val toRemove = ownerStands.keys.filterNot { it in online }
        for (ownerId in toRemove) {
            ownerStands.remove(ownerId)?.remove()
            visibleViewers.remove(ownerId)
            lastHeartText.remove(ownerId)
        }
    }

    fun cleanupPlayer(player: Player) {
        ensureStandRemoved(player)
    }

    fun cleanup() {
        ownerStands.values.forEach { stand ->
            if (!stand.isDead) stand.remove()
        }
        ownerStands.clear()
        visibleViewers.clear()
        lastHeartText.clear()
    }

    override fun cancel() {
        cleanup()
        super.cancel()
    }
}
