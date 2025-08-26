package site.thatkid.soulBound.items.hearts.normal

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.wrappers.EnumWrappers
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import site.thatkid.soulBound.items.HeartRegistry.ghastlyListener
import site.thatkid.soulBound.items.Heart
import site.thatkid.soulBound.items.ItemCreator
import java.util.*
import com.comphenix.protocol.wrappers.Pair as ProtoPair

object Ghastly : Heart() {
    private val plugin: JavaPlugin
        get() = JavaPlugin.getProvidingPlugin(Ghastly::class.java)
    override val key = NamespacedKey(plugin, "ghastly")

    private val cooldowns = mutableMapOf<UUID, Long>()
    private val specialActive = mutableSetOf<UUID>()
    private val cooldownTime = 120_000L

    // Safe ProtocolLib manager - lazy initialization with null safety
    private val protocolManager: ProtocolManager? by lazy {
        try {
            val protocolLibPlugin = Bukkit.getPluginManager().getPlugin("ProtocolLib")
            if (protocolLibPlugin?.isEnabled == true) {
                ProtocolLibrary.getProtocolManager()
            } else {
                plugin.logger.warning("ProtocolLib not found - Ghastly Heart equipment hiding disabled")
                null
            }
        } catch (e: Exception) {
            plugin.logger.warning("Failed to initialize ProtocolLib for Ghastly Heart: ${e.message}")
            null
        }
    }

    // Check if ProtocolLib features are available
    private fun isProtocolLibAvailable(): Boolean = protocolManager != null

    override fun createItem(): ItemStack {
        return ItemCreator.itemCreator(5)
    }

    override fun constantEffect(player: Player) {
        // Apply invisibility in both cases - weak when normal, strong when special is active
        if (specialActive.contains(player.uniqueId)) {
            // During special: stronger invisibility to ensure we stay invisible
            player.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, 100, 0, true, false, false))
        } else {
            // Normal: weak invisibility
            player.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, 60, 0, true, true, false))
        }
    }

    override fun specialEffect(player: Player) {
        val now = System.currentTimeMillis()
        val last = cooldowns[player.uniqueId] ?: 0L
        if (now - last < cooldownTime) {
            val rem = (cooldownTime - (now - last)) / 1000
            player.sendMessage(Component.text("§cGhastly on cooldown: $rem s remaining."))
            return
        }

        // Activate
        cooldowns[player.uniqueId] = now
        specialActive.add(player.uniqueId)

        // Hide the player entity (model, name, armor) from everyone else
        for (other in Bukkit.getOnlinePlayers()) {
            if (other != player) {
                other.hidePlayer(plugin, player)
                // Only send equipment packets if ProtocolLib is available
                if (isProtocolLibAvailable()) {
                    sendEmptyEquipment(player, other)
                }
            }
        }

        // Hide armor from the player's own view (only if ProtocolLib is available)
        if (isProtocolLibAvailable()) {
            sendEmptyEquipmentToSelf(player)
        }

        // Give speed and ensure invisibility
        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 20 * 15, 1, true, true, true))
        player.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, 20 * 15, 0, true, false, false))

        player.sendMessage(Component.text("§5You vanish into the smoke..."))
        player.world.playSound(player.location, Sound.ENTITY_GHAST_SHOOT, 1f, 0.5f)

        // After 15s, restore visibility
        object : BukkitRunnable() {
            override fun run() {
                specialActive.remove(player.uniqueId)
                for (other in Bukkit.getOnlinePlayers()) {
                    if (other != player) {
                        other.showPlayer(plugin, player)
                    }
                }
                // Restore player's own armor view (only if ProtocolLib is available)
                if (isProtocolLibAvailable()) {
                    restoreEquipmentToSelf(player)
                }
            }
        }.runTaskLater(plugin, 20L * 30)
    }

    override fun checkProgress(player: Player): String {
        return ghastlyListener.getProgress(player.uniqueId)
    }

    /**
     * Send "empty" equipment packets so armor slots appear blank to others
     * @param target the player being vanished
     * @param viewer who will receive the packet
     */
    private fun sendEmptyEquipment(target: Player, viewer: Player) {
        val manager = protocolManager ?: return // Safe exit if ProtocolLib unavailable

        val emptySlots = listOf(
            ProtoPair(EnumWrappers.ItemSlot.HEAD, ItemStack(Material.AIR)),
            ProtoPair(EnumWrappers.ItemSlot.CHEST, ItemStack(Material.AIR)),
            ProtoPair(EnumWrappers.ItemSlot.LEGS, ItemStack(Material.AIR)),
            ProtoPair(EnumWrappers.ItemSlot.FEET, ItemStack(Material.AIR))
        )

        try {
            val packet = manager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT)
            packet.integers.write(0, target.entityId)
            packet.slotStackPairLists.write(0, emptySlots)
            manager.sendServerPacket(viewer, packet)
        } catch (e: Exception) {
            plugin.logger.warning("Failed to send empty equipment packet: ${e.message}")
        }
    }

    /**
     * Hide armor from the player's own view by sending empty equipment packet to themselves
     */
    private fun sendEmptyEquipmentToSelf(player: Player) {
        val manager = protocolManager ?: return // Safe exit if ProtocolLib unavailable

        val emptySlots = listOf(
            ProtoPair(EnumWrappers.ItemSlot.HEAD, ItemStack(Material.AIR)),
            ProtoPair(EnumWrappers.ItemSlot.CHEST, ItemStack(Material.AIR)),
            ProtoPair(EnumWrappers.ItemSlot.LEGS, ItemStack(Material.AIR)),
            ProtoPair(EnumWrappers.ItemSlot.FEET, ItemStack(Material.AIR))
            // Don't hide main/offhand from self as it would be confusing
        )

        try {
            val packet = manager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT)
            packet.integers.write(0, player.entityId)
            packet.slotStackPairLists.write(0, emptySlots)
            manager.sendServerPacket(player, packet)
        } catch (e: Exception) {
            plugin.logger.warning("Failed to send empty equipment packet to self: ${e.message}")
        }
    }

    /**
     * Restore the player's armor view to themselves
     */
    private fun restoreEquipmentToSelf(player: Player) {
        val manager = protocolManager ?: return // Safe exit if ProtocolLib unavailable

        val currentEquipment = listOf(
            ProtoPair(EnumWrappers.ItemSlot.HEAD, player.inventory.helmet ?: ItemStack(Material.AIR)),
            ProtoPair(EnumWrappers.ItemSlot.CHEST, player.inventory.chestplate ?: ItemStack(Material.AIR)),
            ProtoPair(EnumWrappers.ItemSlot.LEGS, player.inventory.leggings ?: ItemStack(Material.AIR)),
            ProtoPair(EnumWrappers.ItemSlot.FEET, player.inventory.boots ?: ItemStack(Material.AIR))
        )

        try {
            val packet = manager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT)
            packet.integers.write(0, player.entityId)
            packet.slotStackPairLists.write(0, currentEquipment)
            manager.sendServerPacket(player, packet)
        } catch (e: Exception) {
            plugin.logger.warning("Failed to restore equipment to self: ${e.message}")
        }
    }

    override fun clearCooldown(uuid: UUID) {
        cooldowns.remove(uuid)
        specialActive.remove(uuid)
    }

    override fun getCooldown(playerId: UUID): Long {
        val lastUsed = cooldowns[playerId] ?: return 0L
        val now = System.currentTimeMillis()
        val remaining = cooldownTime - (now - lastUsed)
        return if (remaining > 0) remaining else 0L
    }

    fun isSpecialActive(playerId: UUID): Boolean {
        return specialActive.contains(playerId)
    }
}