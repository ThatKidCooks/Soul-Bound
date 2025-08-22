package site.thatkid.soulBound.managers.hearts.statistic

import kotlinx.coroutines.NonCancellable.cancel
import net.axay.kspigot.runnables.task
import site.thatkid.soulBound.managers.hearts.statistic.listeners.*

class Caller(statistic: Statistic) {

    lateinit var aquaticListener: AquaticListener
    lateinit var golemListener: GolemListener
    lateinit var speedListener: SpeedListener

    val task = task(
        sync = true,
        delay = 25,
        period = 40,
        howOften = null,
        safe = true,
    ) {
        aquaticListener.check(statistic)
        golemListener.check(statistic)
        speedListener.check(statistic)
        // add more statistic based checks later

        if (aquaticListener.received && golemListener.received && speedListener.received) {
            cancel() // stop the task if there is no reason to run it.
        }
    }
}