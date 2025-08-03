package site.thatkid.soulBound.hearts

import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

class ConstantAbilitiesCaller: BukkitRunnable() {
    override fun run() {
        for (player in Bukkit.getOnlinePlayers()) {
            val hearts = ActiveHearts.getHearts(player.uniqueId)
            if (hearts.isEmpty()) continue

            hearts.forEach { heart ->
                heart.constantEffect(player)
            }
        }
    }
}