package site.thatkid.soulBound.managers.hearts.statistic.listeners

import com.google.gson.GsonBuilder
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import site.thatkid.soulBound.items.HeartRegistry
import site.thatkid.soulBound.managers.hearts.statistic.Statistic
import java.io.File

class AquaticListener {

    data class SaveData(
        val received: Boolean = false
    )

    var received = false

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val file = File(plugin.dataFolder, "aquatic.json")

    val plugin: JavaPlugin
        get() = JavaPlugin.getProvidingPlugin(AquaticListener::class.java)

    fun check(statistic: Statistic) {
        for (player in plugin.server.onlinePlayers) {
            val stat = statistic.getStatistic(player, org.bukkit.Statistic.SWIM_ONE_CM)

            if (stat >= (5000 * 100)) {
                if (!received) {
                    val aquaticHeart = HeartRegistry.hearts["aquatic"]?.createItem()
                    if (aquaticHeart == null) return

                    player.inventory.addItem(aquaticHeart)
                    plugin.server.broadcast(Component.text("&c$player was the First Person to swim 5000 blocks and has obtained the Aquatic Heart"))
                    received = true
                    save()
                }
            }
        }
    }

    fun save() {
        try {
            val saveData = SaveData(received) // create a SaveData object with current state
            val json = gson.toJson(saveData) // convert the SaveData object to JSON
            file.parentFile?.mkdirs() // ensure the directory exists
            file.writeText(json) // write the JSON to the file
            plugin.logger.info("Aquatic data saved to ${file.absolutePath}") // log the save
        } catch (ex: Exception) {
            plugin.logger.warning("Failed to save aquatic.json: ${ex.message}")
        }
    }

    fun load() {
        if (!file.exists()) return
        try {
            val json = file.readText()
            val saveData = gson.fromJson(json, SaveData::class.java) // convert the saved JSON to SaveData object
            received = saveData.received // set the received state
            plugin.logger.info("Aquatic data loaded from ${file.absolutePath}") // log the load
        } catch (ex: Exception) {
            plugin.logger.warning("Failed to load aquatic.json: ${ex.message}")
            received = false
        }
    }

    fun getProgress(player: Player): Int {
        val stat = player.getStatistic(org.bukkit.Statistic.SWIM_ONE_CM)
        return (stat / 100)
    }

    fun setGlobalReceived(received: Boolean) {
        this.received = received
    }
}