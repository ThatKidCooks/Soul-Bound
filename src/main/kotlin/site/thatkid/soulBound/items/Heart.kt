package site.thatkid.soulBound.items

import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import site.thatkid.soulBound.hearts.TrustRegistry
import java.util.UUID

abstract class Heart {
    abstract val key: NamespacedKey
    abstract fun createItem(): ItemStack
    abstract fun constantEffect(player: Player)
    abstract fun specialEffect(player: Player)
    abstract fun clearCooldown(playerId: UUID)
    abstract fun getCooldown(playerId: UUID): Long

    open fun trustPlayer(ownerId: UUID, targetId: UUID) {
        TrustRegistry.trust(ownerId, targetId)
    }

    open fun untrustPlayer(ownerId: UUID, targetId: UUID) {
        TrustRegistry.untrust(ownerId, targetId)
    }

    open fun trustList(ownerId: UUID): Set<UUID> {
        return TrustRegistry.getTrusted(ownerId)
    }

    open fun checkProgress(player: Player): String {
        return "§7No progress to show."
    }
}
