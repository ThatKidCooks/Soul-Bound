package site.thatkid.soulBound.managers.hearts.every

import com.google.gson.GsonBuilder
import io.papermc.paper.command.brigadier.argument.ArgumentTypes.player
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
import site.thatkid.soulBound.managers.DiscordBot
import java.io.File
import java.io.IOException
import java.util.*

class WiseListener(private val plugin: JavaPlugin, private val discordBot: DiscordBot) {

    data class SaveData(
        val brewedPotions: MutableMap<UUID, MutableSet<String>> = mutableMapOf(), // Store as String names
        val lastBrewer: MutableMap<String, UUID> = mutableMapOf(),
        val received: Boolean = false
    )

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val file = File(plugin.dataFolder, "wise.json")

    private var brewedPotions: MutableMap<UUID, MutableSet<PotionEffectType>> = mutableMapOf()

    private var received = false

    private var lastBrewer: MutableMap<Location, UUID> = mutableMapOf()

    // Helper functions to convert between Location and String
    private fun locationToString(location: Location): String {
        return "${location.world?.name}:${location.blockX}:${location.blockY}:${location.blockZ}"
    }

    private fun stringToLocation(locationString: String): Location? {
        val parts = locationString.split(":")
        if (parts.size != 4) return null
        val world = Bukkit.getWorld(parts[0]) ?: return null
        return Location(world, parts[1].toDouble(), parts[2].toDouble(), parts[3].toDouble())
    }

    val inventoryClick = listen<InventoryClickEvent> { event ->
        val stand = event.inventory.holder as? BrewingStand ?: return@listen
        val player = event.whoClicked as? Player ?: return@listen
        lastBrewer[stand.location] = player.uniqueId
    }

    val brewEvent = listen<BrewEvent> { event ->
        val brewerId = lastBrewer[event.block.location] ?: return@listen
        val brewer = Bukkit.getPlayer(brewerId) ?: return@listen

        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            val brewingStand = event.block.state as? BrewingStand ?: return@Runnable
            

            for (i in 0..2) {
                val item = brewingStand.inventory.getItem(i) ?: continue
                val meta = item.itemMeta
                if (meta is PotionMeta) {
                    println("Checking final result in slot $i")
                    

                    val effect = when {

                        meta.basePotionType?.effectType != null -> meta.basePotionType!!.effectType

                        meta.hasCustomEffects() -> meta.customEffects.firstOrNull()?.type

                        meta.basePotionData?.type?.effectType != null -> meta.basePotionData!!.type.effectType
                        else -> null
                    }

                    if (effect != null) {
                        println("Potion brewed by ${brewer.name}: $effect") // debug

                        val set = brewedPotions.computeIfAbsent(brewer.uniqueId) { mutableSetOf() }
                        set.add(effect)
                        save()
                    }
                }
            }

            if (!received && (brewedPotions[brewerId]?.count() ?: 0) > 15) {
                plugin.server.broadcast(Component.text("§a${brewer.name} has brewed all possible potion effects and received the Wise Heart!"))
                discordBot.sendMessage("${brewer.name} has brewed all possible potion effects and received the Wise Heart!")

                val wiseHeart = HeartRegistry.hearts["wise"]?.createItem()

                if (wiseHeart != null) {
                    brewer.inventory.addItem(wiseHeart)
                }
                received = true
                save()
            }
        }, 1L)
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
            // Convert Location keys to String keys and PotionEffectType to String names for serialization
            val lastBrewerStrings = lastBrewer.mapKeys { locationToString(it.key) }.toMutableMap()
            val brewedPotionsStrings = brewedPotions.mapValues { (_, effects) ->
                effects.mapNotNull { it.name }.toMutableSet()
            }.toMutableMap()
            
            val saveData = SaveData(brewedPotionsStrings, lastBrewerStrings, received)
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
            
            // Convert String names back to PotionEffectType objects
            brewedPotions = saveData.brewedPotions.mapValues { (_, effectNames) ->
                effectNames.mapNotNull { name ->
                    PotionEffectType.values().find { it.name == name }
                }.toMutableSet()
            }.toMutableMap()
            
            // Convert String keys back to Location keys
            lastBrewer = saveData.lastBrewer.mapNotNull { (locationString, uuid) ->
                stringToLocation(locationString)?.let { it to uuid }
            }.toMap().toMutableMap()
            
            received = saveData.received
            plugin.logger.info("WiseListener data loaded from ${file.absolutePath}")
        } catch (e: IOException) {
            plugin.logger.severe("Failed to load WiseListener data: ${e.message}")
            e.printStackTrace()
        }
    }

    fun getProgress(playerId: UUID): String {
        val effects = brewedPotions[playerId] ?: return "No potions brewed yet."
        val msg: String
        if (effects.isEmpty()) {
            msg = "No potions brewed yet."
        } else {
            msg = "Brewed Potions: ${effects.joinToString(", ") { it.name }}"
        }

        if (received) {
            return "$msg §cThe Wise Heart has already been received by a player."
        }

        return msg
    }

    fun setGlobalReceived(received: Boolean) {
        this.received = received
    }
}