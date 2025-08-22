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
import site.thatkid.soulBound.items.hearts.Wither
import site.thatkid.soulBound.listeners.AutoSave
import java.io.File

/**
 * Main plugin class for the SoulBound SMP Plugin.
 * 
 * This plugin implements a unique heart system where players can obtain different types of hearts
 * by completing specific quests. Each heart provides both passive abilities and special powers.
 * Only one of each heart type can be obtained across the entire server, making them rare and competitive.
 * 
 * Key Features:
 * - 11 unique heart types with different requirements
 * - Passive abilities that are constantly active while holding a heart
 * - Special abilities with cooldowns that can be activated on command
 * - Trust system to prevent friendly fire
 * - Progress tracking for each heart's requirements
 * - Persistent data storage for player progress and active hearts
 * 
 * @author ThatKidCooks
 * @since 1.0-SNAPSHOT
 */
class SoulBound : JavaPlugin() {

    /** GUI component that displays heart information to players */
    private val displayHearts: DisplayHearts = DisplayHearts(this)
    
    /** Auto-save system for periodic data persistence */
    private val autoSave: AutoSave = AutoSave(this)

    /**
     * Plugin initialization - sets up all systems and starts tracking.
     * 
     * This method:
     * 1. Initializes all heart trackers for progress monitoring
     * 2. Sets up the trust system for player relationships
     * 3. Registers command handlers and tab completion
     * 4. Starts periodic tasks (display updates, auto-save, constant effects)
     * 5. Registers all event listeners
     * 6. Loads existing player data from files
     */
    override fun onEnable() {
        // Initialize heart trackers - these monitor quest progress for each heart type
        HeartRegistry.crownedTracker = object : HeartTracker(this, Crowned, killsRequired = 5) {}
        HeartRegistry.crownedTracker.enable()

        HeartRegistry.wardenTracker = WardenHeartTracker(this).apply { enable() }
        HeartRegistry.traderTracker = TraderHeartTracker(this).apply { enable() }
        HeartRegistry.ghastlyTracker = GhastlyHeartTracker(this).apply { enable() }
        HeartRegistry.hasteTracker = HasteHeartTracker(this).apply { enable() }
        HeartRegistry.strengthTracker = StrengthHeartTracker(this).apply { enable() }
        HeartRegistry.aquaticTracker = AquaticHeartTracker(this).apply { enable() }
        HeartRegistry.golemTracker = GolemHeartTracker(this).apply { enable() }
        HeartRegistry.wiseTracker = WiseHeartTracker(this).apply { enable() }
        HeartRegistry.fireTracker = FireHeartTracker(this).apply { enable() }
        HeartRegistry.witherTracker = WitherHeartTracker(this).apply { enable() }
        // Frozen heart tracker is commented out - likely disabled/in development
        //HeartRegistry.frozenTracker = FrozenHeartTracker(this).apply { enable() }

        // Set up trust system for managing player relationships
        HeartRegistry.trustManager = TrustStorageManager
        HeartRegistry.trustManager.load(File(dataFolder, "trusted_players.json"))

        // Create plugin data directory
        val soulBoundDir = File(dataFolder, "Soul Bound").apply { mkdirs() }

        // Register command handlers for /soulbound and /sb
        getCommand("soulbound")?.setExecutor(CommandManager(this, this))
        getCommand("soulbound")?.setTabCompleter(SoulboundTabCompleter())

        // Start periodic tasks
        displayHearts.runTaskTimer(this, 0, 3L) // Update displays every 3 ticks
        autoSave.runTaskTimer(this, 0, 600L) // Auto-save every 30 seconds (600 ticks)
        ConstantAbilitiesCaller().runTaskTimer(this, 0, 20L) // Apply heart effects every second (20 ticks)

        // Register event listeners for various game interactions
        server.pluginManager.registerEvents(HeartEatListener(this), this)
        server.pluginManager.registerEvents(DeathMessageListener(), this)
        server.pluginManager.registerEvents(PlayerDeathListener(this), this)
        server.pluginManager.registerEvents(GolemKBTracker(this), this)
        server.pluginManager.registerEvents(PlayerQuitListener(displayHearts), this)
        server.pluginManager.registerEvents(Wither, this)

        // Load existing player heart data
        ActiveHearts.loadFromFile(File(soulBoundDir, "hearts.json"))
    }

    /**
     * Plugin shutdown - performs cleanup and saves all data.
     * 
     * Ensures all player progress and heart data is saved before the plugin shuts down.
     * Also cleanly disables all heart trackers and clears any active effects.
     */
    override fun onDisable() {
        save()
        HeartRegistry.disableAll()
    }

    /**
     * Saves all plugin data to disk.
     * 
     * This includes:
     * - Active hearts for each player
     * - Progress data for all heart trackers  
     * - Trust relationships between players
     * - Display heart cleanup
     */
    fun save() {
        val soulBoundDir = File(dataFolder, "Soul Bound").apply { mkdirs() }
        
        // Save active hearts data
        ActiveHearts.saveToFile(File(soulBoundDir, "hearts.json"))

        // Save all heart tracker progress
        HeartRegistry.saveAll()

        // Save trust relationships
        HeartRegistry.trustManager.save(File(dataFolder, "trusted_players.json"))
        
        // Clean up display components
        displayHearts.cleanup()
    }
}
