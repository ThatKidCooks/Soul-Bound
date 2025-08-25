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

    override fun createItem(): ItemStack {
        return ItemCreator.itemCreator(14)
    }

    override fun constantEffect(player: Player) {
        spawnParticles(player.location)
        damage(player)
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