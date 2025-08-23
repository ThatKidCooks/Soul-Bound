package site.thatkid.soulBound.gui.admin

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import site.thatkid.soulBound.items.hearts.normal.Aquatic
import site.thatkid.soulBound.items.hearts.normal.Crowned
import site.thatkid.soulBound.items.hearts.normal.Fire
import site.thatkid.soulBound.items.hearts.normal.Frozen
import site.thatkid.soulBound.items.hearts.normal.Ghastly
import site.thatkid.soulBound.items.hearts.normal.Golem
import site.thatkid.soulBound.items.hearts.normal.Haste
import site.thatkid.soulBound.items.hearts.normal.Speed
import site.thatkid.soulBound.items.hearts.normal.Strength
import site.thatkid.soulBound.items.hearts.normal.Trader
import site.thatkid.soulBound.items.hearts.normal.Warden
import site.thatkid.soulBound.items.hearts.normal.Wise
import site.thatkid.soulBound.items.hearts.normal.Wither

class Hearts(private val plugin: JavaPlugin): Listener {
    private val TITLE = Component.text("§bHearts")

    fun open(player: Player) {
        val inventory = Bukkit.createInventory(null, 18, TITLE)

        val crowned = Crowned.createItem()
        val warden = Warden.createItem()
        val trader = Trader.createItem()
        val ghastly = Ghastly.createItem()
        val haste = Haste.createItem()
        val strength = Strength.createItem()
        val aquatic = Aquatic.createItem()
        val golem = Golem.createItem()
        val wise = Wise.createItem()
        val fire = Fire.createItem()
        val wither = Wither.createItem()
        val frozen = Frozen.createItem()
        val speed = Speed.createItem()

        inventory.setItem(0, crowned)
        inventory.setItem(1, warden)
        inventory.setItem(2, trader)
        inventory.setItem(3, ghastly)
        inventory.setItem(4, haste)
        inventory.setItem(5, strength)
        inventory.setItem(6, aquatic)
        inventory.setItem(7, golem)
        inventory.setItem(8, wise)
        inventory.setItem(9, fire)
        inventory.setItem(10, wither)
        inventory.setItem(11, frozen)
        inventory.setItem(12, speed)

        player.openInventory(inventory)
    }
}