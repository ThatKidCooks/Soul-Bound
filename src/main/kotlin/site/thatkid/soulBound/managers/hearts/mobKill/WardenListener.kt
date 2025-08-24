package site.thatkid.soulBound.managers.hearts.mobKill

import com.google.gson.GsonBuilder
import net.axay.kspigot.event.listen
import net.axay.kspigot.event.register
import net.axay.kspigot.event.unregister
import net.axay.kspigot.extensions.broadcast
import org.bukkit.Bukkit
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.plugin.java.JavaPlugin
import site.thatkid.soulBound.items.HeartRegistry
import java.io.File
import java.util.UUID

class WardenListener(private val plugin: JavaPlugin) {

    private data class SaveData (
        val received: Boolean = false
    )

    var received: Boolean = false

    private val file = File(plugin.dataFolder, "warden.json")
    private val gson = GsonBuilder().setPrettyPrinting().create()

    val listener = listen<EntityDeathEvent> {
        val victim = it.entity
        val killer = it.entity.killer ?: return@listen

        if (victim.type.name != "WARDEN") return@listen // only process Warden deaths

        if (!received) {
            val wardenHeart = HeartRegistry.hearts["warden"]?.createItem() // get the Warden Heart item
            if (wardenHeart != null) {
                killer.inventory.addItem(wardenHeart)
                broadcast("The Warden Heart has been awarded to ${killer.name} for killing a Warden First!")
                received = true // no one else can receive the Warden Heart after this
                save() // save the state after giving the heart
            }
        } else {
            killer.sendMessage("§7Someone already received the Warden Heart.") // feedback message
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
            received = saveData.received // set the received state from the loaded data
            plugin.logger.info("Warden data loaded from ${file.absolutePath}") // log the load
        } catch (ex: Exception) {
            plugin.logger.warning("Failed to load warden.json: ${ex.message}")
            received = false
        }
    }

    fun save() {
        try {
            val saveData = SaveData(received) // create a SaveData object with current state
            val json = gson.toJson(saveData) // convert the SaveData object to JSON
            file.parentFile?.mkdirs() // ensure the directory exists
            file.writeText(json) // write the JSON to the file
            plugin.logger.info("Warden data saved to ${file.absolutePath}") // log the save
        } catch (ex: Exception) {
            plugin.logger.warning("Failed to save warden.json: ${ex.message}")
        }
    }

    fun getProgress(playerId: UUID): String { // this seems easy to understand
        val msg = "§${Bukkit.getPlayer(playerId)} hasn't got the Warden Heart yet."

        if (received) {
            return "$msg §cThe Warden heart has already been received by a player."
        }
        return msg
    }

    fun setGlobalReceived(received: Boolean) {
        this.received = received
    }
}