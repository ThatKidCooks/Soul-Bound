package site.thatkid.soulBound.listeners

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector
import site.thatkid.soulBound.items.hearts.Golem

class GolemKBTracker(val plugin: JavaPlugin): Listener {

    @EventHandler
    fun onPlayerDamage(event: EntityDamageByEntityEvent) {
        val player = event.entity as? Player ?: return

        // Check if player has knockback immunity from Golem Heart
        if (Golem.hasKnockbackImmunity(player.uniqueId)) {
            // Cancel knockback by setting velocity to zero after damage
            Bukkit.getScheduler().runTask(plugin, Runnable {
                player.velocity = Vector(0, 0, 0)
            })
        }
    }
}