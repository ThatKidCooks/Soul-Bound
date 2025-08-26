package site.thatkid.soulBound

import HeartEatListener
import net.axay.kspigot.main.KSpigot
import site.thatkid.soulBound.commands.CommandManager
import site.thatkid.soulBound.commands.SoulboundTabCompleter
import site.thatkid.soulBound.gui.player.DisplayHearts
import site.thatkid.soulBound.hearts.ActiveHearts
import site.thatkid.soulBound.hearts.ConstantAbilitiesCaller
import site.thatkid.soulBound.items.HeartRegistry
import site.thatkid.soulBound.items.hearts.normal.Frozen
import site.thatkid.soulBound.listeners.GolemKBTracker
import site.thatkid.soulBound.listeners.PlayerDeathListener
import site.thatkid.soulBound.listeners.PlayerQuitListener
import site.thatkid.soulBound.listeners.msgs.DeathMessageListener
import site.thatkid.soulBound.managers.*
import site.thatkid.soulBound.items.hearts.legendary.Wither
import site.thatkid.soulBound.listeners.AutoSave
import java.io.File

class SoulBound : KSpigot() {


    private val displayHearts: DisplayHearts = DisplayHearts(this)
    private val autoSave: AutoSave = AutoSave(this)

    private val heartRegistry = HeartRegistry

    private lateinit var bridge: DiscordBot

    override fun startup() {
        val BOTIP: String = config.getString("botip") ?: "localhost"
        val BOTPORT: Int = config.getInt("botport")

        bridge = DiscordBot(this, "https://$BOTIP:$BOTPORT")
        bridge.connect()

        heartRegistry.init(this, bridge)
        heartRegistry.enableAll()

        heartRegistry.trustManager = TrustStorageManager

        val soulBoundDir = File(dataFolder, "Soul Bound").apply { mkdirs() }

        heartRegistry.trustManager.load(File(dataFolder, "trusted_players.json"))

        // Command Handlers
        getCommand("soulbound")?.setExecutor(CommandManager(this, this))
        getCommand("soulbound")?.tabCompleter = SoulboundTabCompleter()

        displayHearts.runTaskTimer(this, 0, 3L)
        autoSave.runTaskTimer(this, 0, 600L) // Save every 30 seconds

        ConstantAbilitiesCaller().runTaskTimer(this, 0, 20L)

        server.pluginManager.registerEvents(HeartEatListener(this), this)
        server.pluginManager.registerEvents(DeathMessageListener(), this)
        server.pluginManager.registerEvents(PlayerDeathListener(this), this)
        server.pluginManager.registerEvents(GolemKBTracker(this), this)
        server.pluginManager.registerEvents(PlayerQuitListener(displayHearts), this)
        server.pluginManager.registerEvents(Wither, this) // Needs @EventHandler annotation in Wither object
        server.pluginManager.registerEvents(Frozen, this) // Needs @EventHandler annotation in Frozen object

        ActiveHearts.loadFromFile(File(soulBoundDir, "hearts.json"))

        saveResource("config.yml", false)
        saveDefaultConfig()
    }

    override fun shutdown() {
        save()
        heartRegistry.disableAll()
    }

    fun save() {
        val soulBoundDir = File(dataFolder, "Soul Bound").apply { mkdirs() }
        ActiveHearts.saveToFile(File(soulBoundDir, "hearts.json"))

        heartRegistry.saveAll()

        heartRegistry.trustManager.save(File(dataFolder, "trusted_players.json"))
        displayHearts.cleanup()
    }
}