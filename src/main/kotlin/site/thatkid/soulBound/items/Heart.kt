package site.thatkid.soulBound.items

import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import site.thatkid.soulBound.hearts.TrustRegistry
import java.util.UUID

/**
 * Abstract base class for all SoulBound hearts.
 * 
 * Hearts are special items that provide both passive and active abilities to players.
 * Each heart type has unique requirements to obtain and specific effects when held.
 * Only one of each heart type can exist on the server at any time.
 * 
 * Key Concepts:
 * - **Constant Effect**: Passive ability applied continuously while holding the heart
 * - **Special Effect**: Active ability triggered by the player with a cooldown
 * - **Trust System**: Players can trust others to prevent heart effects from harming them
 * - **Progress Tracking**: Each heart has specific quest requirements to obtain
 * 
 * @see site.thatkid.soulBound.items.hearts for implementations of specific heart types
 */
abstract class Heart {
    /** Unique identifier for this heart type, used for item recognition and data storage */
    abstract val key: NamespacedKey
    
    /**
     * Creates the physical ItemStack representation of this heart.
     * 
     * @return ItemStack with appropriate display name, lore, and persistent data
     */
    abstract fun createItem(): ItemStack
    
    /**
     * Applies the passive/constant effect of this heart to the player.
     * 
     * This method is called repeatedly (every second) while the player has this heart active.
     * Common uses include applying potion effects, checking conditions, or maintaining buffs.
     * 
     * @param player The player who currently has this heart active
     */
    abstract fun constantEffect(player: Player)
    
    /**
     * Triggers the special/active ability of this heart.
     * 
     * This is called when a player uses the `/soulbound ability` command.
     * Most special abilities have cooldowns to prevent spam usage.
     * 
     * @param player The player attempting to use the special ability
     */
    abstract fun specialEffect(player: Player)
    
    /**
     * Clears any active cooldowns for this heart for the specified player.
     * 
     * Typically used by administrators or for testing purposes.
     * 
     * @param playerId UUID of the player whose cooldown should be cleared
     */
    abstract fun clearCooldown(playerId: UUID)
    
    /**
     * Gets the remaining cooldown time for this heart's special ability.
     * 
     * @param playerId UUID of the player to check cooldown for
     * @return Remaining cooldown time in milliseconds, or 0 if no cooldown active
     */
    abstract fun getCooldown(playerId: UUID): Long

    /**
     * Adds a player to the owner's trust list for this heart.
     * 
     * Trusted players will not be affected by harmful heart abilities.
     * For example, trusted players won't take damage from area-of-effect abilities.
     * 
     * @param ownerId UUID of the heart owner
     * @param targetId UUID of the player to trust
     */
    open fun trustPlayer(ownerId: UUID, targetId: UUID) {
        TrustRegistry.trust(ownerId, targetId)
    }

    /**
     * Removes a player from the owner's trust list for this heart.
     * 
     * @param ownerId UUID of the heart owner
     * @param targetId UUID of the player to untrust
     */
    open fun untrustPlayer(ownerId: UUID, targetId: UUID) {
        TrustRegistry.untrust(ownerId, targetId)
    }

    /**
     * Gets the list of players trusted by the heart owner.
     * 
     * @param ownerId UUID of the heart owner
     * @return Set of UUIDs representing trusted players
     */
    open fun trustList(ownerId: UUID): Set<UUID> {
        return TrustRegistry.getTrusted(ownerId)
    }

    /**
     * Gets a formatted string showing the player's progress toward obtaining this heart.
     * 
     * Different hearts have different requirements (kills, blocks mined, trades, etc.)
     * This method should return a user-friendly progress message with colors and formatting.
     * 
     * @param player The player to check progress for
     * @return Formatted progress string with Minecraft color codes
     */
    open fun checkProgress(player: Player): String {
        return "§7No progress to show."
    }
}
