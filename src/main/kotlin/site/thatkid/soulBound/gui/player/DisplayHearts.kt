package site.thatkid.soulBound.gui.player

import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
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

class DisplayHearts : BukkitRunnable() {
    override fun run() {
        for (player in Bukkit.getOnlinePlayers()) {
            val hearts = ActiveHearts.getHearts(player.uniqueId)

            val symbols = mutableListOf<String>()

            for (heart in hearts) {
                when (heart) {
                    is Crowned -> symbols.add("§c❤ Crowned")
                    is Warden -> symbols.add("§1❤ Warden")
                    is Trader -> symbols.add("§a❤ Trader")
                    is Ghastly -> symbols.add("§d❤ Ghastly")
                    is Haste -> symbols.add("§e❤ Haste")
                    is Strength -> symbols.add("§6❤ Strength")
                    is Aquatic -> symbols.add("§b❤ Aquatic")
                    is Golem -> symbols.add("§7❤ Golem")
                    is Wise -> symbols.add("§f❤ Wise")
                    else -> continue
                }
            }

            if (symbols.isNotEmpty()) {
                val combined = symbols.joinToString(" §7| ")
                player.sendActionBar(combined)
            }
        }
    }
}
