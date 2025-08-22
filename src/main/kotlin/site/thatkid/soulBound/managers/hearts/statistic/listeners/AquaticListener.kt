package site.thatkid.soulBound.managers.hearts.statistic.listeners

import net.kyori.adventure.text.Component
import org.bukkit.plugin.java.JavaPlugin
import site.thatkid.soulBound.HeartRegistry
import site.thatkid.soulBound.managers.hearts.statistic.Statistic

class AquaticListener {

    val received = false

    val plugin: JavaPlugin
        get() = JavaPlugin.getProvidingPlugin(AquaticListener::class.java)

    fun check(statistic: Statistic) {
        for (player in plugin.server.onlinePlayers) {
            val stat = statistic.getStatistic(player, org.bukkit.Statistic.SWIM_ONE_CM)

            println(stat)

            if (stat > (5000 / 100)) {
                if (!received) {
                    val aquaticHeart = HeartRegistry.hearts["aquatic"]?.createItem()
                    if (aquaticHeart == null) return

                    player.inventory.addItem(aquaticHeart)
                    plugin.server.broadcast(Component.text("&c$player was the First Person to swim 5000 blocks and has obtained the Aquatic Heart"))
                }
            }
        }
    }

    fun save() {

    }

    fun load() {

    }
}