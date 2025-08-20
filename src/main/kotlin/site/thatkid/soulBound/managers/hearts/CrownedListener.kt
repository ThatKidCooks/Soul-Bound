package site.thatkid.soulBound.managers.hearts

import net.axay.kspigot.event.listen
import net.axay.kspigot.event.register
import net.axay.kspigot.event.unregister
import net.axay.kspigot.extensions.broadcast
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.plugin.java.JavaPlugin
import site.thatkid.soulBound.HeartRegistry
import java.util.UUID

class CrownedListener(private val plugin: JavaPlugin) {

    private val kills: MutableMap<UUID, Int> = mutableMapOf()

    private val recived: Boolean = false

    private val listener = listen<EntityDeathEvent> {
        val player = it.entity.killer ?: return@listen
        val playerId = player.uniqueId
        val currentKills = kills.getOrDefault(playerId, 0) + 1
        kills[playerId] = currentKills

        if (currentKills >= 5) {
            if (!recived) {
                // Give the player a Crowned Heart item
                val crownedHeart = HeartRegistry.hearts["crowned"]?.createItem()
                if (crownedHeart != null) {
                    player.inventory.addItem(crownedHeart)
                    broadcast("The Crowned Heart has been awarded to ${player.name} for killing 5 Players First!")
                }
            }
        } else {
            player.sendMessage("§7You need ${5 - currentKills} more kills to receive a Crowned Heart.")
        }
    }

    fun enable() {
        load()

        listener.register()
    }

    fun disable() {
        listener.unregister()

        save()
    }

    fun load() {
        // load from json
    }

    fun save() {
        // save to json
    }

    fun getProgress(playerId: UUID): Int {
        return kills.getOrDefault(playerId, 0)
    }
}