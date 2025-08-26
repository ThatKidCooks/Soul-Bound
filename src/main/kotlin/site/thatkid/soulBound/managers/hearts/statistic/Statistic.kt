package site.thatkid.soulBound.managers.hearts.statistic

import org.bukkit.Statistic
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player

/**
 * Statistic is a helper class for querying and managing player statistics.
 * 
 * This class provides a simplified interface for working with Bukkit's statistic
 * system, handling the complexity of different statistic types (simple vs entity-specific).
 * It's used by statistic-based heart listeners to check player achievements.
 * 
 * Key Features:
 * - Handles both simple statistics (like distance walked) and entity-specific statistics
 * - Provides a consistent interface for statistic queries
 * - Supports both getting and setting statistic values
 * - Uses sensible defaults for parameter handling
 * 
 * Usage:
 * Used primarily by the Caller system and statistic-based heart listeners
 * to check if players have reached achievement milestones.
 */
class Statistic {

    /**
     * Gets a statistic value for a player.
     * 
     * This method handles both simple statistics (like SWIM_ONE_CM) that don't require
     * an entity type, and entity-specific statistics that do. The default entity type
     * is used as a sentinel value to determine which type of statistic is being queried.
     * 
     * @param player The player to get the statistic for
     * @param statistic The statistic type to query
     * @param entity The entity type (used for entity-specific statistics, ignored for simple ones)
     * @return The statistic value for the player
     */
    fun getStatistic(player: Player, statistic: Statistic, entity: EntityType = EntityType.FALLING_BLOCK): Int {
        
        // Use FALLING_BLOCK as a sentinel value to indicate simple statistics
        if (entity == EntityType.FALLING_BLOCK) {
            return player.getStatistic(statistic) // For statistics that don't require an entity type, like distance walked
        }
        
        return player.getStatistic(statistic, entity) // How did I not know player.getStatistic wasn't a thing lol
    }

    /**
     * Sets a statistic value for a player.
     * 
     * This can be used for administrative purposes, testing, or resetting achievements.
     * Note: Modifying statistics should be done carefully as it affects the player's
     * permanent game data.
     * 
     * @param player The player to set the statistic for
     * @param statistic The statistic type to modify
     * @param value The new value to set
     */
    fun setStatistic(player: Player, statistic: Statistic, value: Int) {
        player.setStatistic(statistic, value)
    }
}