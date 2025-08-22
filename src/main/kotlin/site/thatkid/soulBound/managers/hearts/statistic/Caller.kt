package site.thatkid.soulBound.managers.hearts.statistic

import net.axay.kspigot.runnables.task
import site.thatkid.soulBound.managers.hearts.statistic.listeners.AquaticListener

class Caller(aquaticListener: AquaticListener, statistic: Statistic) {
    val task = task(
        sync = true,
        delay = 25,
        period = 40,
        howOften = null,
        safe = true,
    ) {
        aquaticListener.check(statistic)
        // add more statistic based checks later
    }
}