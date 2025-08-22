package site.thatkid.soulBound.managers.hearts.statistic

import net.axay.kspigot.runnables.task
import site.thatkid.soulBound.managers.hearts.statistic.listeners.*

class Caller(statistic: Statistic) {

    lateinit var aquaticListener: AquaticListener

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