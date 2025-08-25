package site.thatkid.soulBound.items
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
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

object ItemCreator {
    fun keyToHeart(heartIdentifier: Int): NamespacedKey? {
        return when (heartIdentifier) {
            1 -> Aquatic.key
            2 -> Crowned.key
            3 -> Fire.key
            4 -> Frozen.key
            5 -> Ghastly.key
            6 -> Golem.key
            7 -> Haste.key
            8 -> Speed.key
            9 -> Strength.key
            10 -> Trader.key
            11 -> Warden.key
            12 -> Wise.key
            13 -> Wither.key
            else -> null
        }
    }
    fun itemCreator(modelIdentifier: Int): ItemStack {
        val customItem = ItemRegistry.items[modelIdentifier] ?: return ItemStack(org.bukkit.Material.AIR)
        val item = ItemStack(customItem.material, 1)
        item.itemMeta?.setCustomModelData(customItem.modelData)
        val meta = item.itemMeta!!
        meta.displayName(customItem.displayName)
        meta.lore(customItem.lore)
        meta.persistentDataContainer.set(keyToHeart(modelIdentifier)!!, PersistentDataType.BYTE, 1)
        item.itemMeta = meta
        return item
    }
}