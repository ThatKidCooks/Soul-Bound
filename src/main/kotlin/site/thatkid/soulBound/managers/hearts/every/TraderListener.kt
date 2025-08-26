package site.thatkid.soulBound.managers.hearts.every

import com.google.gson.GsonBuilder
import io.papermc.paper.event.player.PlayerTradeEvent
import net.axay.kspigot.event.listen
import net.axay.kspigot.event.register
import net.axay.kspigot.event.unregister
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Villager
import org.bukkit.plugin.java.JavaPlugin
import site.thatkid.soulBound.HeartRegistry
import site.thatkid.soulBound.managers.DiscordBot
import site.thatkid.soulBound.items.HeartRegistry
import java.io.File
import java.util.UUID

class TraderListener(private val plugin: JavaPlugin, private val discordBot: DiscordBot) {

    data class SaveData(
        val received: Boolean = false,
        val villagerTraded: MutableMap<UUID, MutableList<String>> = mutableMapOf()
    )

    private val file = File(plugin.dataFolder, "trader.json")
    private val gson = GsonBuilder().setPrettyPrinting().create()

    private var received = false
    // Store profession keys as strings for serialization
    private var villagerTraded: MutableMap<UUID, MutableList<String>> = mutableMapOf()

    val listener = listen<PlayerTradeEvent> {
        val player = it.player
        val trader = it.villager

        if (trader is Villager) {
            val profKey = trader.profession.key.key // Use the string key
            if (villagerTraded[player.uniqueId] == null) {
                villagerTraded[player.uniqueId] = mutableListOf()
            }
            if (!villagerTraded[player.uniqueId]!!.contains(profKey)) {
                villagerTraded[player.uniqueId]?.add(profKey)
                player.sendMessage("You have traded with a ${trader.profession.key} villager!")
            }

            villagerTraded[player.uniqueId]?.size?.let { size -> if (size >= 13 ) {
                if (received) {
                    player.sendMessage("You have traded with all villager professions however someone has already got the Trader Heart!")
                    return@listen
                }

                plugin.server.broadcast(Component.text("${player.name} has traded with all villager professions and has received the Trader Heart!"))
                discordBot.sendMessage("${player.name} has traded with all villager professions and has received the Trader Heart!")
                val traderHeart = HeartRegistry.hearts["trader"]?.createItem()

                if (traderHeart == null) return@listen

                player.inventory.addItem(traderHeart)
                received = true
                save()
            } }

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
            val saveData = SaveData(received, villagerTraded)
            val json = gson.toJson(saveData)
            file.parentFile.mkdirs()
            file.writeText(json)
            plugin.logger.info("Trader data saved to ${file.absolutePath}")
        } catch (e: Exception) {
            plugin.logger.severe("Failed to save TraderListener data: ${e.message}")
            e.printStackTrace()
        }
    }

    fun load() {
        try {
            if (!file.exists()) return
            val json = file.readText()
            val saveData = gson.fromJson(json, SaveData::class.java)
            received = saveData.received
            villagerTraded = saveData.villagerTraded
            plugin.logger.info("TraderListener data loaded from ${file.absolutePath}")
        } catch (e: Exception) {
            plugin.logger.severe("Failed to load TraderListener data: ${e.message}")
            e.printStackTrace()
        }
    }

    fun getProgress(playerId: UUID): String {
        val msg = "§${Bukkit.getPlayer(playerId)} has gotten the following ${villagerTraded[playerId]?.size} out of 13 villager professions: \n" +
                (villagerTraded[playerId]?.joinToString("\n") { "§a- ${it.key}" } ?: "§cNo professions traded yet.")

        if (received) {
            return "$msg §cThe Trader Heart has already been received by a player."
        }
        return msg
    }

    fun setReceived(received: Boolean) {
        this.received = received
    }
}