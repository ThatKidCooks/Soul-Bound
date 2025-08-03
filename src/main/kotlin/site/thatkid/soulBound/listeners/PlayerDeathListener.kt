package site.thatkid.soulBound.listeners

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.plugin.java.JavaPlugin
import site.thatkid.soulBound.hearts.ActiveHearts

class PlayerDeathListener(private val plugin: JavaPlugin) : Listener {

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val victim = event.entity
        val killer = victim.killer as? Player ?: return

        // Iterate a copy to avoid concurrent modification
        val heartsToDrop = ActiveHearts.getHearts(victim.uniqueId).toList()

        for (heart in heartsToDrop) {
            // Add to vanilla drops so it shows with all other items
            event.drops.add(heart.createItem())

            // Remove from active list immediately
            ActiveHearts.removeHeart(victim, heart)
        }
    }
}
