package site.thatkid.soulBound.items.hearts

import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import site.thatkid.soulBound.items.Heart
import java.util.*

object Haste : Heart() {

    private val plugin: JavaPlugin = JavaPlugin.getProvidingPlugin(Haste::class.java)

    override val key = NamespacedKey(plugin, "haste")

    private val cooldowns = mutableMapOf<UUID, Long>()

    private const val cooldownTime = 90 * 1000L // 1 minute 30 seconds

    override fun createItem(): ItemStack {
        val item = ItemStack(Material.APPLE)
        val meta = item.itemMeta

        meta.persistentDataContainer.set(key, PersistentDataType.BYTE, 1)
        meta.displayName(Component.text("§6Haste Heart"))
        meta.lore(listOf(
            Component.text("§7Obtained by mining 5000 deepslate blocks!"),
            Component.text("§7Grants constant Haste I."),
            Component.text("§aAbility:"),
            Component.text("§f- Haste Surge: Speed III & Haste III for 40s"),
            Component.text("§f- Breaks a 3x3 cube around you")
        ))

        item.itemMeta = meta
        return item
    }

    override fun constantEffect(player: Player) {
        if (!player.hasPotionEffect(PotionEffectType.HASTE) ||
            player.getPotionEffect(PotionEffectType.HASTE)?.amplifier != 0) {
            player.addPotionEffect(PotionEffect(PotionEffectType.HASTE, 40, 0, false, false))
        }
    }

    override fun specialEffect(player: Player) {
        val now = System.currentTimeMillis()

        if (cooldowns[player.uniqueId]?.let { now - it < cooldownTime } == true) {
            val remaining = (cooldownTime - (now - cooldowns[player.uniqueId]!!)) / 1000
            player.sendMessage(Component.text("§cHaste Surge is on cooldown! Wait ${remaining}s."))
            return
        }

        cooldowns[player.uniqueId] = now

        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 800, 2, false, true))
        player.addPotionEffect(PotionEffect(PotionEffectType.HASTE, 800, 2, false, true))
        player.sendMessage(Component.text("§eHaste Surge unleashed! You're lightning fast and ready to dig!"))
        player.playSound(player.location, Sound.ITEM_TOTEM_USE, 1f, 1.4f)

        val centerLoc = player.location.block.location
        val world = player.world

        for (x in -2..1) {
            for (y in -2..1) {
                for (z in -2..1) {
                    val targetLoc = centerLoc.clone().add(x.toDouble(), y.toDouble(), z.toDouble())
                    val targetBlock = world.getBlockAt(targetLoc)

                    if (targetBlock.type != Material.AIR &&
                        targetBlock.type != Material.BEDROCK &&
                        !targetBlock.type.name.contains("PORTAL")) {

                        targetBlock.breakNaturally(player.inventory.itemInMainHand)
                        world.spawnParticle(
                            Particle.EXPLOSION,
                            targetLoc.add(0.5, 0.5, 0.5),
                            8, 0.3, 0.3, 0.3, 0.1
                        )
                        world.playSound(targetLoc, Sound.BLOCK_STONE_BREAK, 1f, 0.8f)
                    }
                }
            }
        }
    }

    override fun clearCooldown(uuid: UUID) {
        cooldowns.remove(uuid)
    }

    override fun getCooldown(playerId: UUID): Long {
        return cooldowns[playerId] ?: 0L
    }
}
