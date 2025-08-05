package site.thatkid.soulBound.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import site.thatkid.soulBound.gui.player.DisplayHearts

class PlayerQuitListener(private val displayHearts: DisplayHearts) : Listener {

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        displayHearts.cleanupPlayer(event.player)
    }

    @EventHandler
    fun onPlayerTeleport(event: PlayerTeleportEvent) {
        // Clean up armor stands when player teleports to different world
        if (event.from.world != event.to?.world) {
            displayHearts.cleanupPlayer(event.player)
        }
    }

    @EventHandler
    fun onPlayerChangedWorld(event: PlayerChangedWorldEvent) {
        // Clean up armor stands when changing worlds
        displayHearts.cleanupPlayer(event.player)
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        // Clean up armor stands when player dies
        displayHearts.cleanupPlayer(event.player)
    }
}