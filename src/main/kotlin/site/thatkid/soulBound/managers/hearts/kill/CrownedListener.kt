package site.thatkid.soulBound.managers.hearts.kill

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

class CrownedListener(private val plugin: JavaPlugin) {

    private data class SaveData (
        val kills: MutableMap<UUID, MutableList<UUID>> = mutableMapOf(),
        val received: Boolean = false
    )

    lateinit var strengthListener: StrengthListener

    private val file = File(plugin.dataFolder, "crowned.json")

    var kills: MutableMap<UUID, MutableList<UUID>> = mutableMapOf()
    private var received: Boolean = false

    val gson = GsonBuilder().setPrettyPrinting().create()

    private val listener = listen<PlayerDeathEvent> {
        val victim = it.entity
        val victimId = victim.uniqueId
        val killer = it.entity.killer ?: return@listen
        val killerId = killer.uniqueId
        val victims = kills.computeIfAbsent(killerId) { mutableListOf() }
        if (!strengthListener.received) { // don't allow Crowned Heart to be given before Strength Heart
            killer.sendMessage("You killed $victim, however this kill will go to the strength heart progress as that hasn't been earned yet.")
            return@listen
        }
        if (!victims.contains(victimId)) {
            victims.add(victimId)
        }

        if (victims.size >= 15) {
            if (!received) {
                // Give the player a Crowned Heart item
                val crownedHeart = HeartRegistry.hearts["crowned"]?.createItem()
                if (crownedHeart != null) {
                    killer.inventory.addItem(crownedHeart)
                    broadcast("The Crowned Heart has been awarded to ${killer.name} for killing 15 Players First!")
                    received = true // no one else can receive the Crowned Heart after this
                    save() // save the state after giving the heart
                }
            } else {
                killer.sendMessage("§7Someone already received the Crowned Heart.") // feedback message
            }
        } else {
            killer.sendMessage("§7You need ${5 - victims.size} more kills to receive the Crowned Heart.") // feedback message
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
        try {
            val json = file.readText()
            val saveData = gson.fromJson(json, SaveData::class.java) // convert the saved JSON to SaveData object
            kills = saveData.kills.toMutableMap() // set the kills map
            received = saveData.received // set the received state
            plugin.logger.info("Crowned data loaded from ${file.absolutePath}") // log the load
        } catch (ex: Exception) {
            plugin.logger.warning("Failed to load crowned.json: ${ex.message}")
            kills = mutableMapOf()
            received = false
        }
    }

    fun save() {
        try {
            val saveData = SaveData(kills, received) // create a SaveData object with current state
            val json = gson.toJson(saveData) // convert the SaveData object to JSON
            file.parentFile?.mkdirs() // ensure the directory exists
            file.writeText(json) // write the JSON to the file
            plugin.logger.info("Crowned data saved to ${file.absolutePath}") // log the save
        } catch (ex: Exception) {
            plugin.logger.warning("Failed to save crowned.json: ${ex.message}")
        }
    }

    fun getProgress(playerId: UUID): String { // this seems easy to understand
        val victims = kills.computeIfAbsent(playerId) { mutableListOf() }
        val total = 5
        val percent = (100 * total / victims.size).coerceAtMost(100)

        return "§${Bukkit.getPlayer(playerId)} has killed §e$kills §7players out of $total. §f($percent%)"
    }

    fun setGlobalReceived(received: Boolean) {
        this.received = received
    }
}