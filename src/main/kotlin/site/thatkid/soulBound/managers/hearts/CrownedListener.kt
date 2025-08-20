package site.thatkid.soulBound.managers.hearts

import com.google.gson.GsonBuilder
import net.axay.kspigot.event.listen
import net.axay.kspigot.event.register
import net.axay.kspigot.event.unregister
import net.axay.kspigot.extensions.broadcast
import org.bukkit.Bukkit
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.plugin.java.JavaPlugin
import site.thatkid.soulBound.HeartRegistry
import java.io.File
import java.util.UUID

class CrownedListener(private val plugin: JavaPlugin) {

    private data class SaveData(
        val kills: MutableMap<UUID, Int>,
        val received: Boolean
    )

    private val file = File(plugin.dataFolder, "crowned.json")

    private var kills: MutableMap<UUID, Int> = mutableMapOf()
    private var received: Boolean = false

    val gson = GsonBuilder().setPrettyPrinting().create()

    private val listener = listen<EntityDeathEvent> {
        val player = it.entity.killer ?: return@listen
        val playerId = player.uniqueId
        val currentKills = kills.getOrDefault(playerId, 0) + 1
        kills[playerId] = currentKills

        if (currentKills >= 5) {
            if (!received) {
                // Give the player a Crowned Heart item
                val crownedHeart = HeartRegistry.hearts["crowned"]?.createItem()
                if (crownedHeart != null) {
                    player.inventory.addItem(crownedHeart)
                    broadcast("The Crowned Heart has been awarded to ${player.name} for killing 5 Players First!")
                    received = true // no one else can receive the Crowned Heart after this
                    save() // save the state after giving the heart
                }
            }
        } else {
            player.sendMessage("§7You need ${5 - currentKills} more kills to receive a Crowned Heart.") // feedback message
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
        plugin.logger.info("Crowned data loaded from ${file.absolutePath}") // log the load
    }

    fun save() {
        val saveData = SaveData(kills, received) // set the data to save
        val json = gson.toJson(saveData) // convert the data to JSON
        if (!file.exists()) file.createNewFile() // create the file if it doesn't exist
        file.writeText(json) // write the JSON to the file
        plugin.logger.info("Crowned data saved to ${file.absolutePath}") // log the save
    }

    fun getProgress(playerId: UUID): String {
        val kills = kills.getOrDefault(playerId, 0)
        val total = 5
        val percent = 100 * total / kills

        return "§${Bukkit.getPlayer(playerId)} has killed §e$kills §7players out of $total. §f($percent%)"
    }

    fun setProgress(playerId: UUID, kills: Int): String {
        this.kills[playerId] = kills // set the kills for the player
        return getProgress(playerId) // return the progress message
    }

    fun setGlobalReceived(received: Boolean) {
        this.received = received
    }
}