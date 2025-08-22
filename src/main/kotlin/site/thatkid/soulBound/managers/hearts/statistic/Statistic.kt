package site.thatkid.soulBound.managers.hearts.statistic

import org.bukkit.Statistic
import org.bukkit.entity.Player

class Statistic {

    fun getStatistic(player: Player, statistic: Statistic): Int {
        val stat = player.getStatistic(statistic) // how did I not know player.getStatistic wasn't a thing lol

        // maybe need to add something if it returns not in blocks - I think it will return in centimetres as it returns Int

        return stat
    }

    fun setStatistic(player: Player, statistic: Statistic, value: Int) {
        player.setStatistic(statistic, value)
    }
}