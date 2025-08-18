package site.thatkid.soulBound.listeners

import org.bukkit.scheduler.BukkitRunnable
import site.thatkid.soulBound.SoulBound

class AutoSave(private val soulBound: SoulBound) : BukkitRunnable() {
    override fun run() {
        soulBound.save()
    }
}