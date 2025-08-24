package site.thatkid.soulBound.managers.hearts.statistic.listeners

import com.google.gson.GsonBuilder
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import site.thatkid.soulBound.HeartRegistry
import site.thatkid.soulBound.managers.DiscordBot
import site.thatkid.soulBound.managers.hearts.mine.FrozenListener
import site.thatkid.soulBound.managers.hearts.mine.FrozenListener.SaveData
import site.thatkid.soulBound.managers.hearts.statistic.Statistic
import java.io.File

class SpeedListener(private val discordBot: DiscordBot) {

    data class SaveData(
        val received: Boolean = false
    )

    var received = false

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val file = File(plugin.dataFolder, "speed.json")

    val plugin: JavaPlugin
        get() = JavaPlugin.getProvidingPlugin(SpeedListener::class.java)

    fun check(statistic: Statistic) {
        for (player in plugin.server.onlinePlayers) {
            val stat = statistic.getStatistic(player, org.bukkit.Statistic.SPRINT_ONE_CM)

            if (stat >= (10000 * 100)) {
                if (!received) {
                    val speedHeart = HeartRegistry.hearts["speed"]?.createItem()
                    if (speedHeart == null) return

                    player.inventory.addItem(speedHeart)
                    plugin.server.broadcast(Component.text("&c$player was the First Person to sprint 10000 blocks and has obtained the Speed Heart"))
                    discordBot.sendMessage("The Speed Heart has been awarded to ${player.name} for sprinting 10000 blocks first!")
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
            plugin.logger.info("Speed data saved to ${file.absolutePath}") // log the save
        } catch (ex: Exception) {
            plugin.logger.warning("Failed to save speed.json: ${ex.message}")
        }
    }

    fun load() {
        if (!file.exists()) return
        try {
            val json = file.readText()
            val saveData = gson.fromJson(json, SaveData::class.java) // convert the saved JSON to SaveData object
            received = saveData.received // set the received state
            plugin.logger.info("Speed data loaded from ${file.absolutePath}") // log the load
        } catch (ex: Exception) {
            plugin.logger.warning("Failed to load speed.json: ${ex.message}")
            received = false
        }
    }

    fun getProgress(player: Player): String {
        val stat = player.getStatistic(org.bukkit.Statistic.SPRINT_ONE_CM)

        val msg = "$player has sprinted ${stat / 100} / 10000 blocks."

        if (received) {
            return "$msg The Speed Heart has already been claimed."
        }

        return msg
    }

    fun setGlobalReceived(received: Boolean) {
        this.received = received
    }
}