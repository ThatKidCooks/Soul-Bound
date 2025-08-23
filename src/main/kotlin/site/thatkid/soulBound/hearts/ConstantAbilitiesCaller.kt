package site.thatkid.soulBound.hearts

import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

/**
 * Periodic task that applies constant heart effects to all online players.
 * 
 * This task runs every second (20 ticks) and ensures that all players with active
 * hearts receive their passive abilities. For example:
 * - Aquatic Heart players get Dolphin's Grace and Conduit Power
 * - Strength Heart players get Strength I
 * - Speed Heart players get Speed effects
 * 
 * The constant effects are applied every second to ensure they don't wear off,
 * as most potion effects have limited durations and need to be continuously renewed.
 * 
 * This system allows each heart to define its own passive abilities through the
 * Heart.constantEffect() method, keeping the code modular and maintainable.
 * 
 * **Usage:** 
 * ```
 * ConstantAbilitiesCaller().runTaskTimer(plugin, 0, 20L) // Every 20 ticks (1 second)
 * ```
 */
class ConstantAbilitiesCaller: BukkitRunnable() {
    
    /**
     * Executes the constant effect application for all online players.
     * 
     * This method:
     * 1. Iterates through all online players
     * 2. Gets their active hearts (if any)
     * 3. Applies the constantEffect() for each heart they have
     * 
     * The constant effects are typically potion effects with short durations
     * that need to be continuously reapplied to maintain the heart's passive abilities.
     */
    override fun run() {
        for (player in Bukkit.getOnlinePlayers()) {
            val hearts = ActiveHearts.getHearts(player.uniqueId)
            if (hearts.isEmpty()) continue

            // Apply constant effects for each heart the player has active
            hearts.forEach { heart ->
                heart.constantEffect(player)
            }
        }
    }
}