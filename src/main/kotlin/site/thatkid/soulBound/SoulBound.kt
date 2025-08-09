package site.thatkid.soulBound

import HeartEatListener
import org.bukkit.plugin.java.JavaPlugin
import site.thatkid.soulBound.commands.CommandManager
import site.thatkid.soulBound.commands.SoulboundTabCompleter
import site.thatkid.soulBound.gui.player.DisplayHearts
import site.thatkid.soulBound.items.hearts.Crowned
import site.thatkid.soulBound.hearts.ActiveHearts
import site.thatkid.soulBound.hearts.ConstantAbilitiesCaller
import site.thatkid.soulBound.listeners.GolemKBTracker
import site.thatkid.soulBound.listeners.PlayerDeathListener
import site.thatkid.soulBound.listeners.PlayerQuitListener
import site.thatkid.soulBound.listeners.msgs.DeathMessageListener
import site.thatkid.soulBound.managers.*
import java.io.File

class SoulBound : JavaPlugin() {

    private lateinit var crownedTracker: HeartTracker
    private lateinit var wardenTracker: WardenHeartTracker
    private lateinit var traderTracker: TraderHeartTracker
    private lateinit var ghastlyTracker: GhastlyHeartTracker
    private lateinit var hasteTracker: HasteHeartTracker
    private lateinit var strengthTracker: StrengthHeartTracker
    private lateinit var aquaticTracker: AquaticHeartTracker
    private lateinit var golemTracker: GolemHeartTracker
    private lateinit var wiseTracker: WiseHeartTracker
    private lateinit var fireTracker: FireHeartTracker
    private lateinit var trustManager: TrustStorageManager

    private var displayHearts: DisplayHearts = DisplayHearts(this)

    override fun onEnable() {
        crownedTracker = object : HeartTracker(this, Crowned, killsRequired = 5) {}
        crownedTracker.enable()

        wardenTracker = WardenHeartTracker(this)
        wardenTracker.enable()

        traderTracker = TraderHeartTracker(this)
        traderTracker.enable()

        ghastlyTracker = GhastlyHeartTracker(this)
        ghastlyTracker.enable()

        hasteTracker = HasteHeartTracker(this)
        hasteTracker.enable()

        strengthTracker = StrengthHeartTracker(this)
        strengthTracker.enable()

        aquaticTracker = AquaticHeartTracker(this)
        aquaticTracker.enable()

        golemTracker = GolemHeartTracker(this)
        golemTracker.enable()

        wiseTracker = WiseHeartTracker(this)
        wiseTracker.enable()

        fireTracker = FireHeartTracker(this)
        fireTracker.enable()

        trustManager = TrustStorageManager
        trustManager.load(File(dataFolder, "trusted_players.json"))

        val soulBoundDir = File(dataFolder, "Soul Bound").apply { mkdirs() }

        // Command Handlers
        getCommand("soulbound")?.setExecutor(CommandManager(this))
        getCommand("soulbound")?.setTabCompleter(SoulboundTabCompleter())

        displayHearts.runTaskTimer(this, 0, 3L)
        ConstantAbilitiesCaller().runTaskTimer(this, 0, 20L)

        server.pluginManager.registerEvents(HeartEatListener(this), this)
        server.pluginManager.registerEvents(DeathMessageListener(), this)
        server.pluginManager.registerEvents(PlayerDeathListener(this), this)
        server.pluginManager.registerEvents(GolemKBTracker(this), this)
        server.pluginManager.registerEvents(PlayerQuitListener(displayHearts), this)

        ActiveHearts.loadFromFile(File(soulBoundDir, "hearts.json"))
    }

    override fun onDisable() {
        val soulBoundDir = File(dataFolder, "Soul Bound").apply { mkdirs() }
        ActiveHearts.saveToFile(File(soulBoundDir, "hearts.json"))

        if (::crownedTracker.isInitialized) crownedTracker.disable()
        if (::wardenTracker.isInitialized) wardenTracker.disable()
        if (::traderTracker.isInitialized) traderTracker.disable()
        if (::ghastlyTracker.isInitialized) ghastlyTracker.disable()
        if (::hasteTracker.isInitialized) hasteTracker.disable()
        if (::strengthTracker.isInitialized) strengthTracker.disable()
        if (::aquaticTracker.isInitialized) aquaticTracker.disable()
        if (::golemTracker.isInitialized) golemTracker.disable()
        if (::wiseTracker.isInitialized) wiseTracker.disable()
        if (::fireTracker.isInitialized) fireTracker.disable()

        trustManager.save(File(dataFolder, "trusted_players.json"))
        displayHearts.cleanup()
    }
}
