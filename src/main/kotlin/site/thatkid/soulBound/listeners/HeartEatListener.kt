import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import site.thatkid.soulBound.SoulBound
import site.thatkid.soulBound.hearts.ActiveHearts
import site.thatkid.soulBound.items.hearts.Aquatic
import site.thatkid.soulBound.items.hearts.Crowned
import site.thatkid.soulBound.items.hearts.Ghastly
import site.thatkid.soulBound.items.hearts.Golem
import site.thatkid.soulBound.items.hearts.Haste
import site.thatkid.soulBound.items.hearts.Strength
import site.thatkid.soulBound.items.hearts.Trader
import site.thatkid.soulBound.items.hearts.Warden
import site.thatkid.soulBound.items.hearts.Wise

class HeartEatListener(private var instance: JavaPlugin) : Listener {

    private val crownedKey = NamespacedKey(instance, "crowned")
    private val wardenKey = NamespacedKey(instance, "warden")
    private val traderKey = NamespacedKey(instance, "trader")
    private val hasteKey = NamespacedKey(instance, "haste")
    private val ghastlyKey = NamespacedKey(instance, "ghastly")
    private val strengthKey = NamespacedKey(instance, "strength")
    private val aquaticKey = NamespacedKey(instance, "aquatic")
    private val golemKey = NamespacedKey(instance, "golem")
    private val wiseKey = NamespacedKey(instance, "wise")

    @EventHandler
    fun onPlayerEat(event: PlayerItemConsumeEvent) {
        val player = event.player
        val item = event.item

        val meta = item.itemMeta ?: return

        if (meta.persistentDataContainer.has(crownedKey, PersistentDataType.BYTE)) {
            ActiveHearts.add(player.uniqueId, Crowned)
        } else if (meta.persistentDataContainer.has(wardenKey, PersistentDataType.BYTE)) {
            ActiveHearts.add(player.uniqueId, Warden)
        } else if (meta.persistentDataContainer.has(traderKey, PersistentDataType.BYTE)) {
            ActiveHearts.add(player.uniqueId, Trader)
        } else if (meta.persistentDataContainer.has(hasteKey, PersistentDataType.BYTE)) {
            ActiveHearts.add(player.uniqueId, Haste)
        } else if (meta.persistentDataContainer.has(ghastlyKey, PersistentDataType.BYTE)) {
            ActiveHearts.add(player.uniqueId, Ghastly)
        } else if (meta.persistentDataContainer.has(strengthKey, PersistentDataType.BYTE)) {
            ActiveHearts.add(player.uniqueId, Strength)
        } else if (meta.persistentDataContainer.has(aquaticKey, PersistentDataType.BYTE)) {
            ActiveHearts.add(player.uniqueId, Aquatic)
        } else if (meta.persistentDataContainer.has(golemKey, PersistentDataType.BYTE)) {
            ActiveHearts.add(player.uniqueId, Golem)
        } else if (meta.persistentDataContainer.has(wiseKey, PersistentDataType.BYTE)) {
            ActiveHearts.add(player.uniqueId, Wise)
        } else {
            return // Not a heart item
        }
    }
}
