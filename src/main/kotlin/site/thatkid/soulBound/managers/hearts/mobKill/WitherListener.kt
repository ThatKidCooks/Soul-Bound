package site.thatkid.soulBound.managers.hearts.mobKill

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

class WitherListener(private val plugin: JavaPlugin) {
    private data class SaveData (
        val withersKilled: MutableMap<UUID, Int> = mutableMapOf(),
        val received: Boolean = false
    )

    lateinit var fireListener: FireListener

    var withersKilled: MutableMap<UUID, Int> = mutableMapOf()
    private var received: Boolean = false

    private val file = File(plugin.dataFolder, "wither.json")
    private val gson = GsonBuilder().setPrettyPrinting().create()

    val listener = listen<EntityDeathEvent> {
        val victim = it.entity
        val killer = it.entity.killer ?: return@listen
        val killerId = killer.uniqueId

        if (victim.type.name != "WITHER") return@listen // only process Wither deaths

        if (!received) {
            if (!fireListener.received) {
                killer.sendMessage("You killed a Wither, however this kill will go to the Fire Heart progress as that hasn't been earned yet.")
                return@listen
            }
            withersKilled[killerId] = withersKilled.getOrDefault(killerId, 0) + 1 // increment the count of Withers killed by the player
            if (withersKilled[killerId]!! < 7) {
                killer.sendMessage("§7You need to kill 7 Withers to receive the Wither Heart. ${7 - withersKilled[killerId]!!} to go!") // feedback message
                return@listen
            }
            val fireHeart = HeartRegistry.hearts["fire"]?.createItem() // get the Fire Heart item
            if (fireHeart != null) {
                killer.inventory.addItem(fireHeart)
                broadcast("The Wither Heart has been awarded to ${killer.name} for killing 7 Withers First!")
                received = true // no one else can receive the Fire Heart after this
                save() // save the state after giving the heart
            }
        } else {
            killer.sendMessage("§7Someone already received the Wither Heart.") // feedback message
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
            withersKilled = saveData.withersKilled // load the withers killed data
            plugin.logger.info("Wither data loaded from ${file.absolutePath}") // log the load
        } catch (ex: Exception) {
            plugin.logger.warning("Failed to load wither.json: ${ex.message}")
            received = false
        }
    }

    fun save() {
        try {
            val saveData = SaveData(withersKilled, received) // create a SaveData object with current state
            val json = gson.toJson(saveData) // convert the SaveData object to JSON
            file.parentFile?.mkdirs() // ensure the directory exists
            file.writeText(json) // write the JSON to the file
            plugin.logger.info("Wither data saved to ${file.absolutePath}") // log the save
        } catch (ex: Exception) {
            plugin.logger.warning("Failed to save wither.json: ${ex.message}")
        }
    }

    fun getProgress(playerId: UUID): String { // this seems easy to understand
        val withersKilledCount = withersKilled[playerId] ?: 0
        val total = 7
        val percent = ((withersKilledCount * 100) / total).coerceAtMost(100)

        val msg = "§${Bukkit.getPlayer(playerId)} has killed $withersKilledCount out of $total Withers. §f($percent%)"

        if (received) {
            return "$msg §cThe Wither heart has already been received by a player."
        }
        return msg
    }

    fun setGlobalReceived(received: Boolean) {
        this.received = received
    }
}