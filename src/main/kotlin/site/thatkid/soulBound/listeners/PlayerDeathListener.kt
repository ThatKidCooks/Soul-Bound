package site.thatkid.soulBound.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.plugin.java.JavaPlugin
import site.thatkid.soulBound.hearts.ActiveHearts

/**
 * Handles player deaths and heart dropping mechanics.
 * 
 * When a player dies while holding a heart, this listener:
 * 1. Creates physical heart items and adds them to the death drops
 * 2. Removes the hearts from the player's active hearts
 * 
 * This ensures that hearts are not lost on death and can be retrieved
 * by the player or potentially stolen by other players, maintaining
 * the competitive nature of the heart system.
 * 
 * @param plugin Reference to the main plugin instance
 */
class PlayerDeathListener(private val plugin: JavaPlugin) : Listener {

    /**
     * Handles player death events to drop hearts as items.
     * 
     * This method ensures that when a player dies:
     * - All their active hearts are converted to physical items
     * - The items are added to their death drops
     * - The hearts are removed from their active hearts list
     * 
     * @param event The player death event containing the victim and drop list
     */
    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val victim = event.entity

        // Get all hearts the player currently has active
        val heartsToDrop = ActiveHearts.getHearts(victim.uniqueId).toList()

        // Convert each heart to a physical item and add to death drops
        for (heart in heartsToDrop) {
            event.drops.add(heart.createItem())
            
            // Remove the heart from the player's active hearts
            ActiveHearts.removeHeart(victim, heart)
        }
    }
}
