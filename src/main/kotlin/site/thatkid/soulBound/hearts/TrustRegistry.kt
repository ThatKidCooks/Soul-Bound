package site.thatkid.soulBound.hearts

import java.util.*

object TrustRegistry {
    val trustedPlayers: MutableMap<UUID, MutableSet<UUID>> = mutableMapOf()

    fun trust(ownerId: UUID, targetId: UUID) {
        val set = trustedPlayers.getOrPut(ownerId) { mutableSetOf() }
        set.add(targetId)
    }

    fun untrust(ownerId: UUID, targetId: UUID) {
        trustedPlayers[ownerId]?.remove(targetId)
    }

    fun getTrusted(ownerId: UUID): Set<UUID> {
        return trustedPlayers[ownerId] ?: emptySet()
    }
}
