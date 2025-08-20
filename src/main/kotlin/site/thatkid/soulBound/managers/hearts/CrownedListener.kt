package site.thatkid.soulBound.managers.hearts

import net.axay.kspigot.event.listen
import net.axay.kspigot.event.register
import net.axay.kspigot.event.unregister
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.plugin.java.JavaPlugin
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