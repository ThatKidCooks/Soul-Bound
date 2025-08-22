package site.thatkid.soulBound

import site.thatkid.soulBound.items.Heart
import site.thatkid.soulBound.items.hearts.*
import site.thatkid.soulBound.managers.*
import javax.net.ssl.TrustManager

/**
 * Central registry for all heart types and their associated trackers.
 * 
 * This singleton object manages:
 * - A map of all available heart types by name
 * - References to all heart progress trackers
 * - Bulk operations for saving/disabling all trackers
 * 
 * The registry allows easy lookup of hearts by string names (used in commands)
 * and provides centralized management of the tracking systems that monitor
 * player progress toward obtaining each heart.
 */
object HeartRegistry {
    /** 
     * Map of heart names to Heart objects for easy command-line lookup.
     * Used by commands like '/soulbound add <player> <heart_name>'
     */
    val hearts: Map<String, Heart> = mapOf(
        "crowned" to Crowned,
        "warden" to Warden,
        "trader" to Trader,
        "ghastly" to Ghastly,
        "haste" to Haste,
        "strength" to Strength,
        "aquatic" to Aquatic,
        "golem" to Golem,
        "wise" to Wise,
        "fire" to Fire,
        "wither" to Wither,
        "frozen" to Frozen,
        "speed" to Speed
    )

    // Heart progress trackers - these monitor quest requirements for each heart type
    lateinit var crownedTracker: HeartTracker
    lateinit var wardenTracker: WardenHeartTracker
    lateinit var traderTracker: TraderHeartTracker
    lateinit var ghastlyTracker: GhastlyHeartTracker
    lateinit var hasteTracker: HasteHeartTracker
    lateinit var strengthTracker: StrengthHeartTracker
    lateinit var aquaticTracker: AquaticHeartTracker
    lateinit var golemTracker: GolemHeartTracker
    lateinit var wiseTracker: WiseHeartTracker
    lateinit var fireTracker: FireHeartTracker
    lateinit var witherTracker: WitherHeartTracker
    lateinit var frozenTracker: FrozenHeartTracker
    
    /** Trust system manager for handling player relationships */
    lateinit var trustManager: TrustStorageManager

    /**
     * Disables all heart trackers, stopping event monitoring and cleaning up resources.
     * 
     * Called during plugin shutdown to ensure all trackers are properly closed
     * and no event handlers remain registered.
     */
    fun disableAll() {
        if (this::crownedTracker.isInitialized) crownedTracker.disable()
        if (this::wardenTracker.isInitialized) wardenTracker.disable()
        if (this::traderTracker.isInitialized) traderTracker.disable()
        if (this::ghastlyTracker.isInitialized) ghastlyTracker.disable()
        if (this::hasteTracker.isInitialized) hasteTracker.disable()
        if (this::strengthTracker.isInitialized) strengthTracker.disable()
        if (this::aquaticTracker.isInitialized) aquaticTracker.disable()
        if (this::golemTracker.isInitialized) golemTracker.disable()
        if (this::wiseTracker.isInitialized) wiseTracker.disable()
        if (this::fireTracker.isInitialized) fireTracker.disable()
        if (this::frozenTracker.isInitialized) frozenTracker.disable()
    }

    /**
     * Saves all heart tracker data to disk.
     * 
     * Ensures all player progress is persisted across server restarts.
     * Called during plugin shutdown and periodic auto-saves.
     */
    fun saveAll() {
        if (this::crownedTracker.isInitialized) crownedTracker.save()
        if (this::wardenTracker.isInitialized) wardenTracker.save()
        if (this::traderTracker.isInitialized) traderTracker.save()
        if (this::ghastlyTracker.isInitialized) ghastlyTracker.save()
        if (this::hasteTracker.isInitialized) hasteTracker.save()
        if (this::strengthTracker.isInitialized) strengthTracker.save()
        if (this::aquaticTracker.isInitialized) aquaticTracker.save()
        if (this::golemTracker.isInitialized) golemTracker.save()
        if (this::wiseTracker.isInitialized) wiseTracker.save()
        if (this::fireTracker.isInitialized) fireTracker.save()
        if (this::frozenTracker.isInitialized) frozenTracker.save()
    }
}
