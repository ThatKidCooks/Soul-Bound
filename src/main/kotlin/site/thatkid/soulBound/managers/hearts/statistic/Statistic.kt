package site.thatkid.soulBound.managers.hearts.statistic

import org.bukkit.Statistic
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player

class Statistic {

    fun getStatistic(player: Player, statistic: Statistic, entity: EntityType = EntityType.FALLING_BLOCK): Int {

        if (entity == EntityType.FALLING_BLOCK) {
            return player.getStatistic(statistic) // for statistics that don't require an entity type, like distance walked
        }

        return player.getStatistic(statistic, entity) // how did I not know player.getStatistic wasn't a thing lol
    }

    fun setStatistic(player: Player, statistic: Statistic, value: Int) {
        player.setStatistic(statistic, value)
    }
}