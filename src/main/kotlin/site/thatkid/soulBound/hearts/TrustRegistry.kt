package site.thatkid.soulBound.hearts

import java.util.*

/**
 * Manages trust relationships between players for heart abilities.
 * 
 * The trust system allows heart holders to designate certain players as "trusted",
 * which prevents their heart abilities from harming those players. This is essential
 * for team play and alliances, as many heart abilities can damage other players.
 * 
 * **Examples of trust protection:**
 * - Crowned Heart's Smash ability won't damage trusted players
 * - Area-of-effect abilities skip trusted targets
 * - Offensive heart powers respect trust relationships
 * 
 * **Trust is one-way:** If Player A trusts Player B, it doesn't automatically
 * mean Player B trusts Player A. Each player manages their own trust list.
 * 
 * Note: This is an in-memory registry. Trust relationships are persisted by
 * the TrustStorageManager class.
 */
object TrustRegistry {
    /** Maps each player UUID to their set of trusted player UUIDs */
    val trustedPlayers: MutableMap<UUID, MutableSet<UUID>> = mutableMapOf()

    /**
     * Adds a player to another player's trust list.
     * 
     * @param ownerId UUID of the player who is granting trust
     * @param targetId UUID of the player being trusted
     */
    fun trust(ownerId: UUID, targetId: UUID) {
        val set = trustedPlayers.getOrPut(ownerId) { mutableSetOf() }
        set.add(targetId)
    }

    /**
     * Removes a player from another player's trust list.
     * 
     * @param ownerId UUID of the player who is revoking trust
     * @param targetId UUID of the player being untrusted
     */
    fun untrust(ownerId: UUID, targetId: UUID) {
        trustedPlayers[ownerId]?.remove(targetId)
    }

    /**
     * Gets the set of all players trusted by a specific player.
     * 
     * @param ownerId UUID of the player whose trust list to retrieve
     * @return Set of UUIDs representing trusted players, or empty set if none
     */
    fun getTrusted(ownerId: UUID): Set<UUID> {
        return trustedPlayers[ownerId] ?: emptySet()
    }
}
