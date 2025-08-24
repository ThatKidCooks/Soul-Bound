import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import site.thatkid.soulBound.hearts.ActiveHearts
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

class HeartEatListener(private val plugin: JavaPlugin) : Listener {

    private val keys = mapOf(
        "crowned" to Crowned,
        "warden" to Warden,
        "trader" to Trader,
        "haste" to Haste,
        "ghastly" to Ghastly,
        "strength" to Strength,
        "aquatic" to Aquatic,
        "golem" to Golem,
        "wise" to Wise,
        "fire" to Fire,
        "wither" to Wither,
        "frozen" to Frozen,
        "speed" to Speed
    ).mapKeys { NamespacedKey(plugin, it.key) }

    @EventHandler
    fun onPlayerRightClick(event: PlayerInteractEvent) {

        if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return

        val item = event.item ?: return
        if (item.type != Material.APPLE) return

        val meta = item.itemMeta ?: return
        val container = meta.persistentDataContainer

        val matchedHeart = keys.entries.firstOrNull { (key, _) ->
            container.has(key, PersistentDataType.BYTE)
        }?.value ?: return

        val result = ActiveHearts.add(event.player.uniqueId, matchedHeart)

        when (result) {
            ActiveHearts.AddHeartResult.SUCCESS -> {
                event.player.sendMessage("§aHeart consumed!")
                event.player.inventory.removeItem(item.asOne())
            }
            ActiveHearts.AddHeartResult.COOLDOWN_ACTIVE -> {
                event.isCancelled = true
                event.player.sendMessage("§cYou cannot switch hearts yet — cooldown active!")
            }
            ActiveHearts.AddHeartResult.SAME_HEART -> {
                event.isCancelled = true
                event.player.sendMessage("§eYou already have this heart.")
            }
        }

    }
}
