package site.thatkid.soulBound.items.hearts.legendary

import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import site.thatkid.soulBound.hearts.TrustRegistry
import site.thatkid.soulBound.items.Heart
import site.thatkid.soulBound.items.ItemCreator
import java.util.UUID

object Dragon: Heart() {

    private val plugin: JavaPlugin
        get() = JavaPlugin.getProvidingPlugin(Dragon::class.java)

    override val key: NamespacedKey
        get() = NamespacedKey(plugin, "dragon")

    private const val RADIUS = 3.0

    private val cooldowns = mutableMapOf<UUID, Long>()
    private val cooldownTime = 120 * 1000L // 2 minutes in milliseconds

    override fun createItem(): ItemStack {
        return ItemCreator.itemCreator(14)
    }

    override fun constantEffect(player: Player) {
        spawnParticles(player.location)
        damage(player)
    }

    override fun specialEffect(player: Player) {
        val now = System.currentTimeMillis()
        val lastUsed = cooldowns[player.uniqueId] ?: 0L
        if (now - lastUsed < cooldownTime) {
            val remaining = (cooldownTime - (now - lastUsed)) / 1000
            player.sendMessage("§cDragon ability is on cooldown! Wait $remaining seconds.")
            return
        }
        cooldowns[player.uniqueId] = now
        // TODO: Implement actual dragon special effect logic here
    }

    override fun clearCooldown(playerId: UUID) {
        cooldowns.remove(playerId)
    }

    override fun getCooldown(playerId: UUID): Long {
        val lastUsed = cooldowns[playerId] ?: return 0L
        val now = System.currentTimeMillis()
        val remaining = cooldownTime - (now - lastUsed)
        return if (remaining > 0) remaining else 0L
    }

    fun spawnParticles(spawn: Location) {

        spawn.world.spawnParticle(Particle.DRAGON_BREATH, spawn, 1)
    }

    fun damage(player: Player) {
        for (players in player.world.getNearbyPlayers(player.location, RADIUS)) {
            if (TrustRegistry.getTrusted(player.uniqueId).contains(players.uniqueId)) return

            players.damage(1.0, player)
        }
    }
}