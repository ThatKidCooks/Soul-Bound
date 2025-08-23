package site.thatkid.soulBound.managers.hearts.every

import com.google.gson.GsonBuilder
import net.axay.kspigot.event.listen
import net.axay.kspigot.event.register
import net.axay.kspigot.event.unregister
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.BrewingStand
import org.bukkit.entity.Player
import org.bukkit.event.inventory.BrewEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffectType
import site.thatkid.soulBound.HeartRegistry
import java.io.File
import java.io.IOException
import java.util.*

class WiseListener(private val plugin: JavaPlugin) {

    data class SaveData(
        val brewedPotions: MutableMap<UUID, MutableSet<PotionEffectType>> = mutableMapOf(),
        val lastBrewer: MutableMap<Location, UUID> = mutableMapOf(),
        val received: Boolean = false
    )

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val file = File(plugin.dataFolder, "wise.json")

    private var brewedPotions: MutableMap<UUID, MutableSet<PotionEffectType>> = mutableMapOf()

    private var received = false

    private var lastBrewer: MutableMap<Location, UUID> = mutableMapOf()

    val inventoryClick = listen<InventoryClickEvent> { event ->
        val stand = event.inventory.holder as? BrewingStand ?: return@listen
        val player = event.whoClicked as? Player ?: return@listen
        lastBrewer[stand.location] = player.uniqueId
    }

    val brewEvent = listen<BrewEvent> { event ->
        val brewerId = lastBrewer[event.block.location] ?: return@listen
        val brewer = Bukkit.getPlayer(brewerId) ?: return@listen

        for (item in event.contents) {
            if (item == null) continue
            val meta = item.itemMeta
            if (meta is PotionMeta) {
                val effect = meta.basePotionType?.effectType ?: continue

                println("Potion brewed by ${brewer.name}: $effect") // debug

                val set = brewedPotions.computeIfAbsent(brewer.uniqueId) { mutableSetOf() }
                set.add(effect)
            }
        }

        val craftablePotionEffects = PotionEffectType.values()
            .filterNotNull().toSet()

        if (!received && brewedPotions.values.any { it.containsAll(craftablePotionEffects) }) {
            plugin.server.broadcast(Component.text("§a${brewer.name} has brewed all possible potion effects and received the Wise Heart!"))
            val wiseHeart = HeartRegistry.hearts["wise"]?.createItem()

            if (wiseHeart != null) {
                brewer.inventory.addItem(wiseHeart)
            }
            received = true
            save()
        }
    }

    fun enable() {
        inventoryClick.register()
        brewEvent.register()
        load()
    }

    fun disable() {
        inventoryClick.unregister()
        brewEvent.unregister()
        save()
    }

    fun save() {
        try {
            val saveData = SaveData(brewedPotions, lastBrewer, received)
            val json = gson.toJson(saveData)
            file.parentFile.mkdirs()
            file.writeText(json)
            plugin.logger.info("WiseListener data saved to ${file.absolutePath}")
        } catch (e: Exception) {
            plugin.logger.severe("Failed to save WiseListener data: ${e.message}")
            e.printStackTrace()
        }
    }

    fun load() {
        try {
            if (!file.exists()) return
            val json = file.readText()
            val saveData = gson.fromJson(json, SaveData::class.java)
            brewedPotions = saveData.brewedPotions
            lastBrewer = saveData.lastBrewer
            received = saveData.received
            plugin.logger.info("WiseListener data loaded from ${file.absolutePath}")
        } catch (e: IOException) {
            plugin.logger.severe("Failed to load WiseListener data: ${e.message}")
            e.printStackTrace()
        }
    }
}