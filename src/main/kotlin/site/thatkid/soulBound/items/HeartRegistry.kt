package site.thatkid.soulBound

import org.bukkit.plugin.java.JavaPlugin
import site.thatkid.soulBound.items.Heart
import site.thatkid.soulBound.items.hearts.normal.Aquatic
import site.thatkid.soulBound.items.hearts.rare.Crowned
import site.thatkid.soulBound.items.hearts.normal.Fire
import site.thatkid.soulBound.items.hearts.normal.Frozen
import site.thatkid.soulBound.items.hearts.normal.Ghastly
import site.thatkid.soulBound.items.hearts.normal.Golem
import site.thatkid.soulBound.items.hearts.normal.Haste
import site.thatkid.soulBound.items.hearts.normal.Speed
import site.thatkid.soulBound.items.hearts.normal.Strength
import site.thatkid.soulBound.items.hearts.normal.Trader
import site.thatkid.soulBound.items.hearts.legendary.Warden
import site.thatkid.soulBound.items.hearts.rare.Wise
import site.thatkid.soulBound.items.hearts.legendary.Wither
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
    private lateinit var discordBot: DiscordBot

    lateinit var hearts: Map<String, Heart>
        private set

    fun init(plugin: JavaPlugin, discordBot: DiscordBot) {
        this.discordBot = discordBot
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
            crownedListener = CrownedListener(plugin, discordBot)
        }
        if (!this::strengthListener.isInitialized) {
            strengthListener = StrengthListener(plugin, discordBot)
        }
        if (!this::hasteListener.isInitialized) {
            hasteListener = HasteListener(plugin, discordBot)
        }
        if (!this::fireListener.isInitialized) {
            fireListener = FireListener(plugin, discordBot)
        }
        if (!this::frozenListener.isInitialized) {
            frozenListener = FrozenListener(plugin, discordBot)
        }
        if (!this::aquaticListener.isInitialized) {
            aquaticListener = AquaticListener(discordBot)
        }
        if (!this::golemListener.isInitialized) {
            golemListener = GolemListener(discordBot)
        }
        if (!this::ghastlyListener.isInitialized) {
            ghastlyListener = GhastlyListener(plugin, discordBot)
        }
        if (!this::speedListener.isInitialized) {
            speedListener = SpeedListener(discordBot)
        }
        if (!this::wiseListener.isInitialized) {
            wiseListener = WiseListener(plugin, discordBot)
        }
        if (!this::traderListener.isInitialized) {
            traderListener = TraderListener(plugin, discordBot)
        }

        // just for crowned and strength, they are linked
        crownedListener.strengthListener = strengthListener
        strengthListener.crownedListener = crownedListener

        // enable the listeners
        crownedListener.enable()
        strengthListener.enable()
        hasteListener.enable()
        fireListener.enable()
        frozenListener.enable()
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
        if (this::frozenListener.isInitialized) frozenListener.load()
        if (this::aquaticListener.isInitialized) aquaticListener.load()
        if (this::golemListener.isInitialized) golemListener.load()
        if (this::ghastlyListener.isInitialized) ghastlyListener.load()
        if (this::speedListener.isInitialized) speedListener.load()
        if (this::wiseListener.isInitialized) wiseListener.load()
        if (this::traderListener.isInitialized) traderListener.load()
    }
}
