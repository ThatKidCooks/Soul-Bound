package site.thatkid.soulBound

import org.bukkit.plugin.java.JavaPlugin
import site.thatkid.soulBound.items.Heart
import site.thatkid.soulBound.items.hearts.normal.Aquatic
import site.thatkid.soulBound.items.hearts.normal.Crowned
import site.thatkid.soulBound.items.hearts.normal.Fire
import site.thatkid.soulBound.items.hearts.normal.Frozen
import site.thatkid.soulBound.items.hearts.normal.Ghastly
import site.thatkid.soulBound.items.hearts.normal.Golem
import site.thatkid.soulBound.items.hearts.normal.Haste
import site.thatkid.soulBound.items.hearts.normal.Speed
import site.thatkid.soulBound.items.hearts.normal.Strength
import site.thatkid.soulBound.items.hearts.normal.Trader
import site.thatkid.soulBound.items.hearts.normal.Warden
import site.thatkid.soulBound.items.hearts.normal.Wise
import site.thatkid.soulBound.items.hearts.normal.Wither
import site.thatkid.soulBound.managers.*
import site.thatkid.soulBound.managers.hearts.advancments.GhastlyListener
import site.thatkid.soulBound.managers.hearts.every.TraderListener
import site.thatkid.soulBound.managers.hearts.kill.*
import site.thatkid.soulBound.managers.hearts.mine.*
import site.thatkid.soulBound.managers.hearts.mobKill.*
import site.thatkid.soulBound.managers.hearts.every.WiseListener
import site.thatkid.soulBound.managers.hearts.statistic.Caller
import site.thatkid.soulBound.managers.hearts.statistic.Statistic
import site.thatkid.soulBound.managers.hearts.statistic.listeners.*

object HeartRegistry {

    private lateinit var plugin: JavaPlugin

    lateinit var hearts: Map<String, Heart>
        private set

    fun init(plugin: JavaPlugin) {
        this.plugin = plugin

        hearts = mapOf(
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
    }

    lateinit var crownedListener: CrownedListener
    lateinit var strengthListener: StrengthListener
    lateinit var hasteListener: HasteListener
    lateinit var fireListener: FireListener
    lateinit var witherListener: WitherListener
    lateinit var wardenListener: WardenListener
    lateinit var frozenListener: FrozenListener
    lateinit var aquaticListener: AquaticListener
    lateinit var golemListener: GolemListener
    lateinit var ghastlyListener: GhastlyListener
    lateinit var speedListener: SpeedListener
    lateinit var wiseListener: WiseListener
    lateinit var traderListener: TraderListener

    lateinit var statistic: Statistic
    lateinit var caller: Caller

    lateinit var trustManager: TrustStorageManager

    fun enableAll() {
        // Initialize the listeners if they are not already initialized
        if (!this::crownedListener.isInitialized) {
            crownedListener = CrownedListener(plugin)
        }
        if (!this::strengthListener.isInitialized) {
            strengthListener = StrengthListener(plugin)
        }
        if (!this::hasteListener.isInitialized) {
            hasteListener = HasteListener(plugin)
        }
        if (!this::fireListener.isInitialized) {
            fireListener = FireListener(plugin)
        }
        if (!this::witherListener.isInitialized) {
            witherListener = WitherListener(plugin)
        }
        if (!this::frozenListener.isInitialized) {
            frozenListener = FrozenListener(plugin)
        }
        if (!this::wardenListener.isInitialized) {
            wardenListener = WardenListener(plugin)
        }
        if (!this::aquaticListener.isInitialized) {
            aquaticListener = AquaticListener()
        }
        if (!this::golemListener.isInitialized) {
            golemListener = GolemListener()
        }
        if (!this::ghastlyListener.isInitialized) {
            ghastlyListener = GhastlyListener(plugin)
        }
        if (!this::speedListener.isInitialized) {
            speedListener = SpeedListener()
        }
        if (!this::wiseListener.isInitialized) {
            wiseListener = WiseListener(plugin)
        }
        if (!this::traderListener.isInitialized) {
            traderListener = TraderListener(plugin)
        }

        // just for crowned and strength, they are linked
        crownedListener.strengthListener = strengthListener
        strengthListener.crownedListener = crownedListener
        // just for wither and fire, they are linked
        fireListener.witherListener = witherListener
        witherListener.fireListener = fireListener

        // enable the listeners
        crownedListener.enable()
        strengthListener.enable()
        hasteListener.enable()
        fireListener.enable()
        witherListener.enable()
        frozenListener.enable()
        wardenListener.enable()
        ghastlyListener.enable()
        wiseListener.enable()
        traderListener.enable()

        // set statistic and caller
        statistic = Statistic()
        caller = Caller(statistic)

        // do statistic based hearts
        caller.aquaticListener = aquaticListener
        caller.golemListener = golemListener
        caller.speedListener = speedListener

        caller.task
    }


    fun disableAll() {
        if (this::crownedListener.isInitialized) crownedListener.disable()
        if (this::strengthListener.isInitialized) strengthListener.disable()
        if (this::hasteListener.isInitialized) hasteListener.disable()
        if (this::fireListener.isInitialized) fireListener.disable()
        if (this::witherListener.isInitialized) witherListener.disable()
        if (this::wardenListener.isInitialized) wardenListener.disable()
        if (this::frozenListener.isInitialized) frozenListener.disable()
        if (this::aquaticListener.isInitialized) aquaticListener.save()
        if (this::golemListener.isInitialized) golemListener.save()
        if (this::ghastlyListener.isInitialized) ghastlyListener.disable()
        if (this::wiseListener.isInitialized) wiseListener.disable()
        if (this::traderListener.isInitialized) traderListener.disable()

        // disable caller task
        caller.task?.cancel()
    }

    fun saveAll() {
        if (this::crownedListener.isInitialized) crownedListener.save()
        if (this::strengthListener.isInitialized) strengthListener.save()
        if (this::hasteListener.isInitialized) hasteListener.save()
        if (this::fireListener.isInitialized) fireListener.save()
        if (this::witherListener.isInitialized) witherListener.save()
        if (this::wardenListener.isInitialized) wardenListener.save()
        if (this::frozenListener.isInitialized) frozenListener.save()
        if (this::aquaticListener.isInitialized) aquaticListener.save()
        if (this::golemListener.isInitialized) golemListener.save()
        if (this::ghastlyListener.isInitialized) ghastlyListener.save()
        if (this::speedListener.isInitialized) speedListener.save()
        if (this::wiseListener.isInitialized) wiseListener.save()
        if (this::traderListener.isInitialized) traderListener.save()
    }

    fun loadAll() {
        if (this::crownedListener.isInitialized) crownedListener.load()
        if (this::strengthListener.isInitialized) strengthListener.load()
        if (this::hasteListener.isInitialized) hasteListener.load()
        if (this::fireListener.isInitialized) fireListener.load()
        if (this::witherListener.isInitialized) witherListener.load()
        if (this::wardenListener.isInitialized) wardenListener.load()
        if (this::frozenListener.isInitialized) frozenListener.load()
        if (this::aquaticListener.isInitialized) aquaticListener.load()
        if (this::golemListener.isInitialized) golemListener.load()
        if (this::ghastlyListener.isInitialized) ghastlyListener.load()
        if (this::speedListener.isInitialized) speedListener.load()
        if (this::wiseListener.isInitialized) wiseListener.load()
        if (this::traderListener.isInitialized) traderListener.load()
    }
}
