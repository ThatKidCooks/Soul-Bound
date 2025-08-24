package site.thatkid.soulBound.managers.hearts.mine

import com.google.gson.GsonBuilder
import net.axay.kspigot.event.listen
import net.axay.kspigot.event.register
import net.axay.kspigot.event.unregister
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.plugin.java.JavaPlugin
import site.thatkid.soulBound.items.HeartRegistry
import java.io.File
import java.util.UUID

class FrozenListener(private val plugin: JavaPlugin) {
    private val file = File(plugin.dataFolder, "frozen.json")

    var blocksMined: MutableMap<UUID, Int> = mutableMapOf()
    private var received: Boolean = false

    val gson = GsonBuilder().setPrettyPrinting().create()

    data class SaveData(
        val blocksMined: MutableMap<UUID, Int> = mutableMapOf(),
        val received: Boolean = false
    )

    private val iceTypes = setOf(
        Material.ICE,
        Material.BLUE_ICE,
        Material.PACKED_ICE,
        Material.FROSTED_ICE
    )

    private val listener = listen<BlockBreakEvent> {
        val player = it.player
        val playerId = player.uniqueId
        val blocks = blocksMined.computeIfAbsent(playerId) { 0 }
        if (!iceTypes.contains(it.block.type)) return@listen
        blocksMined[playerId] = blocks + 1 // increment the block count for the player

        if (blocksMined[playerId]!! >= 10000) { // check if the player has mined enough blocks
            if (!received) {
                // Give the player a Frozen Heart item
                val frozenHeart = HeartRegistry.hearts["frozen"]?.createItem()
                if (frozenHeart != null) {
                    player.inventory.addItem(frozenHeart)
                    Bukkit.broadcastMessage("The Frozen Heart has been awarded to ${player.name} for mining 10,000 Ice Blocks First!")
                    received = true // no one else can receive the Frozen Heart after this
                    save() // save the state after giving the heart
                }
            } else {
                player.sendMessage("§7Someone already received the Frozen Heart.") // feedback message
            }
        } else {
            player.sendMessage("§7You need ${100 - blocksMined[playerId]!!} more blocks to receive the Frozen Heart.") // feedback message
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
            blocksMined = saveData.blocksMined.toMutableMap() // set the kills map
            received = saveData.received // set the received state
            plugin.logger.info("Frozen data loaded from ${file.absolutePath}") // log the load
        } catch (ex: Exception) {
            plugin.logger.warning("Failed to load frozen.json: ${ex.message}")
            blocksMined = mutableMapOf()
            received = false
        }
    }

    fun save() {
        try {
            val saveData = SaveData(blocksMined, received) // create a SaveData object with current state
            val json = gson.toJson(saveData) // convert the SaveData object to JSON
            file.parentFile?.mkdirs() // ensure the directory exists
            file.writeText(json) // write the JSON to the file
            plugin.logger.info("Frozen data saved to ${file.absolutePath}") // log the save
        } catch (ex: Exception) {
            plugin.logger.warning("Failed to save frozen.json: ${ex.message}")
        }
    }

    fun getProgress(playerId: UUID): String { // this seems easy to understand
        val blocks = blocksMined.computeIfAbsent(playerId) { 0 }
        val total = 10000
        val percent = ((blocks * 100) / total).coerceAtMost(100)

        val msg = "§${Bukkit.getPlayer(playerId)} has mined §e$blocks §blocks out of $total. §f($percent%)"

        if (received) {
            return "$msg §cThe Frozen heart has already been received by a player."
        }
        return msg
    }

    fun setGlobalReceived(received: Boolean) {
        this.received = received
    }
}