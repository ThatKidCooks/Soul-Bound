package site.thatkid.soulBound.gui.admin

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import site.thatkid.soulBound.items.hearts.legendary.Dragon
import site.thatkid.soulBound.items.hearts.normal.Aquatic
import site.thatkid.soulBound.items.hearts.rare.Crowned
import site.thatkid.soulBound.items.hearts.normal.Fire
import site.thatkid.soulBound.items.hearts.normal.Frozen
import site.thatkid.soulBound.items.hearts.normal.Ghastly
import site.thatkid.soulBound.items.hearts.normal.Golem
import site.thatkid.soulBound.items.hearts.normal.Haste
import site.thatkid.soulBound.items.hearts.normal.Speed
import site.thatkid.soulBound.items.hearts.normal.Strength
import site.thatkid.soulBound.items.hearts.normal.Trader
import site.thatkid.soulBound.items.hearts.legendary.Warden
import site.thatkid.soulBound.items.hearts.rare.Wise
import site.thatkid.soulBound.items.hearts.legendary.Wither

class Hearts(private val plugin: JavaPlugin): Listener {
    private val TITLE = Component.text("§bHearts")

    fun open(player: Player) {
        val inventory = Bukkit.createInventory(null, 27, TITLE)

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
        val dragon = Dragon.createItem()

        inventory.setItem(0, trader)
        inventory.setItem(1, ghastly)
        inventory.setItem(2, haste)
        inventory.setItem(3, strength)
        inventory.setItem(4, aquatic)
        inventory.setItem(5, golem)
        inventory.setItem(6, fire)
        inventory.setItem(7, frozen)
        inventory.setItem(8, speed)
        inventory.setItem(9, wise)
        inventory.setItem(10, crowned)
        inventory.setItem(18, wither)
        inventory.setItem(19, warden)
        inventory.setItem(20, dragon)

        player.openInventory(inventory)
    }
}