package site.thatkid.soulBound.managers.hearts.statistic

import kotlinx.coroutines.NonCancellable.cancel
import net.axay.kspigot.runnables.task
import site.thatkid.soulBound.managers.hearts.statistic.listeners.*

/**
 * Caller manages the periodic checking system for statistic-based hearts.
 * 
 * This class creates and manages a background task that periodically checks
 * player statistics to determine if any statistic-based hearts should be awarded.
 * Unlike event-based hearts that respond to specific game events, statistic-based
 * hearts require periodic polling of Bukkit's statistic system.
 * 
 * Key Features:
 * - Runs a periodic task every 2 seconds (40 ticks) after a 1.25 second delay
 * - Calls check() methods on all registered statistic-based heart listeners
 * - Automatically stops the task when all statistic-based hearts have been awarded
 * - Provides centralized management for all statistic-based heart systems
 * 
 * Performance Optimization:
 * The task automatically cancels itself once all registered hearts have been
 * awarded, preventing unnecessary CPU usage on completed achievements.
 * 
 * @param statistic The Statistic helper instance for querying player data
 */
class Caller(statistic: Statistic) {

    /** Listener for swimming distance-based Aquatic Heart */
    lateinit var aquaticListener: AquaticListener
    
    /** Listener for iron golem interaction-based Golem Heart */
    lateinit var golemListener: GolemListener
    
    /** Listener for movement distance-based Speed Heart */
    lateinit var speedListener: SpeedListener

    /**
     * Background task that periodically checks statistic-based hearts.
     * 
     * Task Configuration:
     * - sync = true: Runs on main thread for thread safety
     * - delay = 25: Wait 1.25 seconds before first execution
     * - period = 40: Run every 2 seconds (40 ticks)
     * - howOften = null: Run indefinitely until manually cancelled
     * - safe = true: Handle exceptions gracefully
     * 
     * The task checks all registered statistic listeners and automatically
     * cancels itself when all hearts have been awarded to optimize performance.
     */
    val task = task(
        sync = true,
        delay = 25,        // 1.25 seconds delay before first run
        period = 40,       // Run every 2 seconds
        howOften = null,   // Run indefinitely
        safe = true,       // Handle exceptions safely
    ) {
        // Check all statistic-based heart listeners
        aquaticListener.check(statistic)
        golemListener.check(statistic)
        speedListener.check(statistic)
        // Add more statistic based checks later
        
        // Performance optimization: Stop the task if all hearts have been awarded
        // This prevents unnecessary CPU usage once all achievements are complete
        if (aquaticListener.received && golemListener.received && speedListener.received) {
            cancel() // Stop the task if there is no reason to run it
        }
    }
}