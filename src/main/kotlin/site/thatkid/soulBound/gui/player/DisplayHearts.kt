package site.thatkid.soulBound.gui.player

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import site.thatkid.soulBound.hearts.ActiveHearts
import site.thatkid.soulBound.items.hearts.*
import java.util.*

class DisplayHearts : BukkitRunnable() {

    private val playerArmorStands = mutableMapOf<UUID, ArmorStand>()
    private val lastHeartText = mutableMapOf<UUID, String>()

    override fun run() {
        for (player in Bukkit.getOnlinePlayers()) {
            val hearts = ActiveHearts.getHearts(player.uniqueId)
            val symbols = mutableListOf<String>()

            val detailedSymbols = mutableListOf<String>()
            for (heart in hearts) {
                when (heart) {
                    is Crowned -> detailedSymbols.add("§c❤ Crowned")
                    is Warden -> detailedSymbols.add("§1❤ Warden")
                    is Trader -> detailedSymbols.add("§a❤ Trader")
                    is Ghastly -> detailedSymbols.add("§d❤ Ghastly")
                    is Haste -> detailedSymbols.add("§e❤ Haste")
                    is Strength -> detailedSymbols.add("§6❤ Strength")
                    is Aquatic -> detailedSymbols.add("§b❤ Aquatic")
                    is Golem -> detailedSymbols.add("§7❤ Golem")
                    is Wise -> detailedSymbols.add("§f❤ Wise")
                }
            }

            val detailedText = if (detailedSymbols.isNotEmpty()) detailedSymbols.joinToString(" §7| ") else ""

            if (detailedText.isNotEmpty()) {
                player.sendActionBar("§r$detailedText")
            }

            val shouldHideHearts = isPlayerInvisible(player)
            if (!shouldHideHearts) {
                updateFloatingText(player, detailedText)
            } else {
                updateFloatingText(player, "")
            }
        }
    }

    private fun isPlayerInvisible(player: Player): Boolean {
        val hearts = ActiveHearts.getHearts(player.uniqueId)
        val ghastlyHeart = hearts.find { it is Ghastly } as? Ghastly

        return if (ghastlyHeart != null) {
            true
        } else {
            false
        }
    }

    private fun updateFloatingText(player: Player, text: String) {
        val currentStand = playerArmorStands[player.uniqueId]
        if (currentStand != null && !currentStand.isDead) {
            currentStand.remove()
        }
        playerArmorStands.remove(player.uniqueId)

        if (text.isEmpty()) {
            lastHeartText.remove(player.uniqueId)
            return
        }

        val loc = player.location.clone().add(0.0, 2.3, 0.0)
        val armorStand = player.world.spawnEntity(loc, EntityType.ARMOR_STAND) as ArmorStand

        armorStand.isVisible = false
        armorStand.isSmall = true
        armorStand.setGravity(false)
        armorStand.isMarker = true
        armorStand.customName = text
        armorStand.isCustomNameVisible = true
        armorStand.setCanPickupItems(false)
        armorStand.setBasePlate(false)
        armorStand.setArms(false)

        playerArmorStands[player.uniqueId] = armorStand
        lastHeartText[player.uniqueId] = text
    }

    fun cleanupPlayer(player: Player) {
        playerArmorStands[player.uniqueId]?.let { armorStand ->
            armorStand.remove()
            playerArmorStands.remove(player.uniqueId)
        }
        lastHeartText.remove(player.uniqueId)
    }

    fun cleanup() {
        playerArmorStands.values.forEach { it.remove() }
        playerArmorStands.clear()
        lastHeartText.clear()
    }

    override fun cancel() {
        cleanup()
        super.cancel()
    }
}