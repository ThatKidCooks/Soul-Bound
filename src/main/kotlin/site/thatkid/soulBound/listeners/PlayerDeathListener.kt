package site.thatkid.soulBound.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.plugin.java.JavaPlugin
import site.thatkid.soulBound.hearts.ActiveHearts

class PlayerDeathListener(private val plugin: JavaPlugin) : Listener {

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val victim = event.entity

        val heartsToDrop = ActiveHearts.getHearts(victim.uniqueId).toList()

        for (heart in heartsToDrop) {
            event.drops.add(heart.createItem())

            ActiveHearts.removeHeart(victim, heart)
        }
    }
}
