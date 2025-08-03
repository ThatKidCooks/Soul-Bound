package site.thatkid.soulBound.listeners.msgs

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import site.thatkid.soulBound.items.hearts.Crowned
import site.thatkid.soulBound.items.hearts.Warden
import net.kyori.adventure.text.Component

class DeathMessageListener : Listener {

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val victim = event.entity
        val uuid = victim.uniqueId

        // First check Crowned
        Crowned.smashedBy.remove(uuid)?.let { killerUUID ->
            val killer = Bukkit.getPlayer(killerUUID)
            if (killer != null) {
                event.deathMessage(Component.text("${victim.name} was smashed by ${killer.name}"))
            }
            return
        }

        // Then check Warden
        Warden.obliteratedBy.remove(uuid)?.let { killerUUID ->
            val killer = Bukkit.getPlayer(killerUUID)
            if (killer != null) {
                event.deathMessage(Component.text("${victim.name} was obliterated by ${killer.name}"))
            }
        }
    }
}
