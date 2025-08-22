package site.thatkid.soulBound.managers.hearts.advancments

import com.google.gson.GsonBuilder
import net.axay.kspigot.event.listen
import net.axay.kspigot.event.register
import net.axay.kspigot.event.unregister
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.advancement.Advancement
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.plugin.java.JavaPlugin
import site.thatkid.soulBound.HeartRegistry
import java.io.File
import java.util.UUID

class GhastlyListener(private val plugin: JavaPlugin) {

    data class SaveData(
        val returnDone: MutableMap<UUID, Boolean> = mutableMapOf(),
        val uneasyDone: MutableMap<UUID, Boolean> = mutableMapOf(),
        val received: Boolean = false
    )

    private var returnDone: MutableMap<UUID, Boolean> = mutableMapOf()
    private var uneasyDone: MutableMap<UUID, Boolean> = mutableMapOf()

    private var received = false

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val file = File(plugin.dataFolder, "ghastly.json")

    val listener = listen<PlayerAdvancementDoneEvent> {

        val player = it.player
        val playerId = player.uniqueId
        val advancement = it.advancement.key.key

        if (advancement == "nether/return_to_sender") returnDone[playerId] = true
        if (advancement == "nether/uneasy_alliance") uneasyDone[playerId] = true

        if (returnDone[playerId] == true && uneasyDone[playerId] == true) {
            if (!received) {
                val ghastlyHeart = HeartRegistry.hearts["ghastly"]?.createItem()
                if (ghastlyHeart != null) {
                    player.inventory.addItem(ghastlyHeart)
                    player.server.broadcast(Component.text("§aThe Ghastly Heart has been awarded to ${player.name} for completing the required advancements!"))
                    received = true // no one else can receive the Ghastly Heart after this
                }
            } else {
                player.sendMessage("§cA player has already received the Ghastly Heart.")
            }
        } else {
           player.sendMessage("You need to complete both advancements: Return to Sender and Uneasy Alliance to unlock the Ghastly Heart.")
        }
    }

    fun enable() {
        listener.register()
        load()
    }

    fun disable() {
        listener.unregister()
        save()
    }

    fun save() {
        try {
            val saveData = SaveData(returnDone, uneasyDone, received) // create a SaveData object with current state
            val json = gson.toJson(saveData) // convert the SaveData object to JSON
            file.parentFile?.mkdirs() // ensure the directory exists
            file.writeText(json) // write the JSON to the file
            plugin.logger.info("Ghastly data saved to ${file.absolutePath}") // log the save
        } catch (ex: Exception) {
            plugin.logger.warning("Failed to save ghastly.json: ${ex.message}")
        }
    }

    fun load() {
        try {
            if (!file.exists()) return
            val json = file.readText()
            val saveData = gson.fromJson(json, SaveData::class.java) // convert the saved JSON to SaveData object
            returnDone = saveData.returnDone // set the returnDone map
            uneasyDone = saveData.uneasyDone // set the uneasyDone map
            received = saveData.received // set the received state
            plugin.logger.info("Ghastly data loaded from ${file.absolutePath}") // log the load
        } catch (ex: Exception) {
            plugin.logger.warning("Failed to load ghastly.json: ${ex.message}")
            returnDone = mutableMapOf()
            uneasyDone = mutableMapOf()
            received = false
        }
    }

    fun getProgress(playerId: UUID): String { // this seems easy to understand
        val msg = "§${Bukkit.getPlayer(playerId)} has gotten the following advancements for the Ghastly Heart: \n)" +
                "§aReturn to Sender: ${if (returnDone[playerId] == true) "§aDone" else "§cNot Done"}\n" +
                "§aUneasy Alliance: ${if (uneasyDone[playerId] == true) "§aDone" else "§cNot Done"}"

        if (received) {
            return "$msg §cThe Ghastly Heart has already been received by a player."
        }
        return msg
    }

    fun setGlobalReceived(received: Boolean) {
        this.received = received
    }
}