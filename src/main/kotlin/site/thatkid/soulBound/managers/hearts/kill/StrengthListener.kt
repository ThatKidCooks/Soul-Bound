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
import site.thatkid.soulBound.managers.DiscordBot
import java.io.File
import java.util.UUID

class StrengthListener(private val plugin: JavaPlugin, private val discordBot: DiscordBot) {

    private data class SaveData(
        val kills: MutableMap<UUID, MutableList<UUID>> = mutableMapOf(),
        val received: Boolean = false
    )

    lateinit var crownedListener: CrownedListener

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
                // Give the player a Strength Heart item
                val strengthHeart = HeartRegistry.hearts["strength"]?.createItem()
                if (strengthHeart != null) {
                    killer.inventory.addItem(strengthHeart)
                    broadcast("The Strength Heart has been awarded to ${killer.name} for killing 10 Players First!")
                    discordBot.sendMessage("The Strength Heart has been awarded to ${killer.name} for killing 10 Players First!")
                    received = true // no one else can receive the Strength Heart after this
                    save() // save the state after giving the heart
                    crownedListener.kills.clear() // needs to get 15 kills after getting the strength heart.
                }
            } else {
                killer.sendMessage("§7Someone already received the Strength Heart.") // feedback message
            }
        } else {
            killer.sendMessage("§7You need ${10 - victims.size} more kills to receive the Strength Heart.") // feedback message
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
            plugin.logger.info("Strength data loaded from ${file.absolutePath}") // log the load
        } catch (ex: Exception) {
            plugin.logger.warning("Failed to load strength.json: ${ex.message}")
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
            plugin.logger.info("Strength data saved to ${file.absolutePath}") // log the save
        } catch (ex: Exception) {
            plugin.logger.warning("Failed to save strength.json: ${ex.message}")
        }
    }

    fun getProgress(playerId: UUID): String {
        val victims = kills.computeIfAbsent(playerId) { mutableListOf() }
        val total = 10
        val percent = (100 * total / victims.size).coerceAtMost(100)

        val msg = "§${Bukkit.getPlayer(playerId)} has killed §e$kills §7players out of $total. §f($percent%)"

        if (received) {
            return "$msg §cThe Strength heart has already been received by a player."
        }
        return msg
    }

    fun setGlobalReceived(received: Boolean) {
        this.received = received
    }
}
