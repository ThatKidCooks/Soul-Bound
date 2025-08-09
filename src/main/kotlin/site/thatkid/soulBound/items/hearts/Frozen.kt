package site.thatkid.soulBound.items.hearts

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import site.thatkid.soulBound.items.Heart
import java.util.UUID

object Frozen : Heart() {
    private val plugin: JavaPlugin = JavaPlugin.getProvidingPlugin(Frozen::class.java)

    private val cooldowns = mutableMapOf<UUID, Long>()
    private var remaining: Long = 0L
    private var cooldownTime = 100 * 1000L // 20 seconds in milliseconds

    override val key: NamespacedKey
        get() = NamespacedKey(plugin, "frozen")

    override fun createItem(): ItemStack {
        val item = ItemStack(Material.APPLE)
        val meta = item.itemMeta!!
        meta.displayName(Component.text("§1Frozen"))
        meta.lore(listOf(
            Component.text("§7Born in the Icy Lakes"),
            Component.text(""),
            Component.text("§f✧ §7Permanent §cFire Resistance §7& §9Strength when on Fire"),
            Component.text(""),
            Component.text("§3§lPower — Lava Surge"),
            Component.text("§cLaunch Enemies into the Air & Set the Ground on Fire"),
            Component.text("§8Cooldown: 100 seconds")
        ))
        meta.persistentDataContainer.set(key, PersistentDataType.BYTE, 1)
        item.itemMeta = meta
        return item
    }

    override fun constantEffect(player: Player) {
        TODO("Not yet implemented")
    }

    override fun specialEffect(player: Player) {
        TODO("Not yet implemented")
    }

    override fun clearCooldown(playerId: UUID) {
        TODO("Not yet implemented")
    }

    override fun getCooldown(playerId: UUID): Long {
        TODO("Not yet implemented")
    }
}