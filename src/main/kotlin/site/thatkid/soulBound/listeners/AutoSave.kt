package site.thatkid.soulBound.listeners

import org.bukkit.scheduler.BukkitRunnable
import site.thatkid.soulBound.SoulBound

/**
 * Automatic save system for the SoulBound plugin.
 * 
 * This periodic task ensures that all plugin data is regularly saved to disk,
 * preventing data loss in case of unexpected server shutdowns or crashes.
 * 
 * **What gets saved:**
 * - Active hearts for all players
 * - Progress data for all heart trackers
 * - Trust relationships between players
 * - All server world data (via save-all command)
 * 
 * The auto-save runs every 30 seconds (600 ticks) by default and is essential
 * for maintaining data integrity in the competitive heart system where progress
 * and hearts are valuable and difficult to obtain.
 * 
 * **Usage:**
 * ```
 * AutoSave(plugin).runTaskTimer(plugin, 0, 600L) // Every 30 seconds
 * ```
 * 
 * @param soulBound Reference to the main plugin instance for access to save functionality
 */
class AutoSave(private val soulBound: SoulBound) : BukkitRunnable() {
    
    /**
     * Performs the automatic save operation.
     * 
     * This method:
     * 1. Saves all SoulBound plugin data (hearts, progress, trust relationships)
     * 2. Executes the server's save-all command to save world data
     * 3. Logs the save operation for monitoring
     * 
     * The save operation is designed to be quick and non-blocking to avoid
     * affecting server performance during regular gameplay.
     */
    override fun run() {
        // Save all plugin data
        soulBound.save()
        
        // Save all server world data
        soulBound.server.dispatchCommand(soulBound.server.consoleSender, "save-all")
        
        // Log successful save
        soulBound.logger.info("Auto-saved SoulBound data and server state.")
    }
}