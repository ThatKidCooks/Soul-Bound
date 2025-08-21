package site.thatkid.soulBound.managers.hearts

import com.google.gson.GsonBuilder
import net.axay.kspigot.event.listen
import net.axay.kspigot.event.register
import net.axay.kspigot.event.unregister
import net.axay.kspigot.extensions.broadcast
import org.bukkit.Bukkit
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.plugin.java.JavaPlugin
import site.thatkid.soulBound.HeartRegistry
import java.io.File
import java.util.UUID

class StrengthListener(private val plugin: JavaPlugin, private val crownedListener: CrownedListener) {

    private data class SaveData(
        val kills: MutableMap<UUID, MutableList<UUID>>,
        val received: Boolean
    )

    private val file = File(plugin.dataFolder, "strength.json")

    private var kills: MutableMap<UUID, MutableList<UUID>> = mutableMapOf()
    var received: Boolean = false

    val gson = GsonBuilder().setPrettyPrinting().create()

    private val listener = listen<PlayerDeathEvent> {
        val victim = it.entity
        val victimId = victim.uniqueId
        val killer = it.entity.killer ?: return@listen
        val killerId = killer.uniqueId
        val victims = kills.computeIfAbsent(killerId) { mutableListOf() }
        if (!victims.contains(victimId)) {
            victims.add(victimId)
        }

        if (victims.size >= 10) {
            if (!received) {
                // Give the player a Crowned Heart item
                val strengthHeart = HeartRegistry.hearts["strength"]?.createItem()
                if (strengthHeart != null) {
                    killer.inventory.addItem(strengthHeart)
                    broadcast("The Strength Heart has been awarded to ${killer.name} for killing 10 Players First!")
                    received = true // no one else can receive the Crowned Heart after this
                    save() // save the state after giving the heart
                    crownedListener.kills.clear() // needs to get 15 kills after getting the strength heart.
                }
            }
        } else {
            killer.sendMessage("§7You need ${5 - victims.size} more kills to receive the Strength Heart.") // feedback message
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
        if (!file.exists()) return

        val json = file.readText() // read the JSON from the file
        val saveData = gson.fromJson(json, SaveData::class.java) // convert the JSON to SaveData object
        kills = saveData.kills.toMutableMap() // set the kills map
        received = saveData.received // set the received status
        plugin.logger.info("Strength data loaded from ${file.absolutePath}") // log the load
    }

    fun save() {
        val saveData = SaveData(kills, received) // set the data to save
        val json = gson.toJson(saveData) // convert the data to JSON
        if (!file.exists()) file.createNewFile() // create the file if it doesn't exist
        file.writeText(json) // write the JSON to the file
        plugin.logger.info("Strength data saved to ${file.absolutePath}") // log the save
    }

    fun getProgress(playerId: UUID): String {
        val victims = kills.computeIfAbsent(playerId) { mutableListOf() }
        val total = 15
        val percent = 100 * total / victims.size

        if (received) {
            return "§cThe Strength heart has already been received."
        }

        return "§${Bukkit.getPlayer(playerId)} has killed §e$kills §7players out of $total. §f($percent%)"
    }

    fun setGlobalReceived(received: Boolean) {
        this.received = received
    }
}