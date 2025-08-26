package site.thatkid.soulBound.items

import org.bukkit.plugin.java.JavaPlugin
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
import site.thatkid.soulBound.managers.hearts.advancments.GhastlyListener
import site.thatkid.soulBound.managers.hearts.every.TraderListener
import site.thatkid.soulBound.managers.hearts.kill.*
import site.thatkid.soulBound.managers.hearts.mine.*
import site.thatkid.soulBound.managers.hearts.mobKill.*
import site.thatkid.soulBound.managers.hearts.every.WiseListener
import site.thatkid.soulBound.managers.hearts.statistic.Caller
import site.thatkid.soulBound.managers.hearts.statistic.Statistic
import site.thatkid.soulBound.managers.hearts.statistic.listeners.*
import site.thatkid.soulBound.managers.TrustStorageManager

/**
 * HeartRegistry is the central management system for all Heart items and their associated listeners.
 * 
 * This object serves as the main coordination point for the Soul-Bound plugin's heart system.
 * It manages the initialization, enabling, disabling, and persistence of all heart-related
 * functionality across the plugin.
 * 
 * Key Responsibilities:
 * - Maintains a registry of all available Heart items
 * - Manages all heart listener instances (event handlers for earning hearts)
 * - Coordinates the initialization and lifecycle of heart systems
 * - Handles bulk operations for save/load/enable/disable across all hearts
 * - Manages statistic-based hearts through the Caller system
 * 
 * Heart Types Managed:
 * - Event-based hearts (kill, mine, brew, advancement-based)
 * - Statistic-based hearts (swimming, mob damage, movement)
 * - Special linked hearts (crowned/strength pair)
 * 
 * @see Heart Base interface for all heart items
 * @see Statistic For player statistic tracking
 * @see Caller For periodic statistic checks
 */
object HeartRegistry {

    /** Reference to the main plugin instance for logging and file operations */
    private lateinit var plugin: JavaPlugin
    private lateinit var discordBot: DiscordBot

    /** 
     * Registry mapping heart names to their corresponding Heart objects.
     * This allows lookup of hearts by string keys for commands and configuration.
     */
    lateinit var hearts: Map<String, Heart>
        private set
        
    /** Manager for trust relationships between players (for certain heart mechanics) */
    lateinit var trustmanager: TrustStorageManager
    /**
     * Initializes the heart registry with the plugin instance and creates the hearts mapping.
     * Must be called during plugin initialization before any hearts can be used.
     * 
     * @param plugin The main plugin instance
     */
    fun init(plugin: JavaPlugin) {
        this.plugin = plugin

        // Create the mapping of heart names to their corresponding objects
        // This allows easy lookup by string keys for commands, configs, etc.
        hearts = mapOf(
            "crowned" to Crowned,      // Rare: Awarded for PvP kills
            "warden" to Warden,        // Legendary: Awarded for defeating the Warden
            "trader" to Trader,        // Normal: Awarded for trading activities
            "ghastly" to Ghastly,      // Normal: Awarded for Nether advancements
            "haste" to Haste,          // Normal: Awarded for mining deepslate
            "strength" to Strength,    // Normal: Linked with crowned heart
            "aquatic" to Aquatic,      // Normal: Awarded for swimming distance
            "golem" to Golem,          // Normal: Awarded for iron golem interactions
            "wise" to Wise,            // Rare: Awarded for brewing all potion effects
            "fire" to Fire,            // Normal: Awarded for fire/lava related activities
            "wither" to Wither,        // Legendary: Awarded for defeating the Wither
            "frozen" to Frozen,        // Normal: Awarded for ice/snow related activities
            "speed" to Speed           // Normal: Awarded for movement distance
        )
    }

    // ===== EVENT-BASED HEART LISTENERS =====
    // These listeners handle specific game events to award hearts
    
    /** Manages PvP kill tracking for the Crowned Heart (rare) */
    lateinit var crownedListener: CrownedListener
    
    /** Manages strength-based activities (linked with crowned heart) */
    lateinit var strengthListener: StrengthListener
    
    /** Manages deepslate block mining for the Haste Heart */
    lateinit var hasteListener: HasteListener
    
    /** Manages fire/lava related activities for the Fire Heart */
    lateinit var fireListener: FireListener
    /** Manages ice/snow related activities for the Frozen Heart */
    lateinit var frozenListener: FrozenListener
    
    /** Manages swimming distance statistics for the Aquatic Heart */
    lateinit var aquaticListener: AquaticListener
    
    /** Manages iron golem interaction statistics for the Golem Heart */
    lateinit var golemListener: GolemListener
    
    /** Manages Nether advancement completion for the Ghastly Heart */
    lateinit var ghastlyListener: GhastlyListener
    
    /** Manages movement distance statistics for the Speed Heart */
    lateinit var speedListener: SpeedListener
    
    /** Manages potion brewing completion for the Wise Heart (rare) */
    lateinit var wiseListener: WiseListener
    
    /** Manages trading activities for the Trader Heart */
    lateinit var traderListener: TraderListener

    // ===== STATISTIC MANAGEMENT SYSTEM =====
    
    /** Handles Bukkit statistic queries and calculations */
    lateinit var statistic: Statistic
    
    /** Periodically calls statistic-based listeners to check for heart awards */
    lateinit var caller: Caller

    /** Manages player trust relationships for certain heart mechanics */
    lateinit var trustManager: TrustStorageManager

    /**
     * Initializes and enables all heart listeners and systems.
     * 
     * This is the main startup method that:
     * 1. Initializes all listener instances if not already created
     * 2. Sets up special relationships between linked hearts
     * 3. Registers all event listeners
     * 4. Configures the statistic-based heart system
     * 5. Starts the periodic caller task for statistic checks
     * 
     * Should be called during plugin enable phase.
     */
    fun enableAll() {
        // ===== INITIALIZE EVENT-BASED LISTENERS =====
        // Only create instances if they don't already exist (prevents double initialization)
        
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

        // ===== CONFIGURE SPECIAL RELATIONSHIPS =====
        // The crowned and strength hearts are linked - obtaining one affects the other
        crownedListener.strengthListener = strengthListener
        strengthListener.crownedListener = crownedListener

        // ===== ENABLE ALL EVENT-BASED LISTENERS =====
        // Register event handlers and load existing data
        crownedListener.enable()
        strengthListener.enable()
        hasteListener.enable()
        fireListener.enable()
        frozenListener.enable()
        ghastlyListener.enable()
        wiseListener.enable()
        traderListener.enable()

        // ===== CONFIGURE STATISTIC SYSTEM =====
        // Set up the periodic checking system for statistic-based hearts
        statistic = Statistic()
        caller = Caller(statistic)

        // Associate statistic-based listeners with the caller
        caller.aquaticListener = aquaticListener
        caller.golemListener = golemListener
        caller.speedListener = speedListener

        // Start the periodic task that checks statistics and awards hearts
        caller.task
    }

    /**
     * Disables all heart listeners and stops background tasks.
     * 
     * This cleanup method:
     * 1. Unregisters all event listeners
     * 2. Saves current progress for event-based hearts
     * 3. Saves current progress for statistic-based hearts
     * 4. Cancels the periodic caller task
     * 
     * Should be called during plugin disable phase to ensure proper cleanup.
     */
    fun disableAll() {
        // Disable event-based listeners (unregister events and save data)
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

        // Stop the periodic statistics checking task
        caller.task?.cancel()
    }

    /**
     * Saves all heart progress data to their respective files.
     * 
     * This can be called periodically or during shutdown to persist
     * all player progress across server restarts. Safe to call even
     * if some listeners aren't initialized.
     */
    fun saveAll() {
        // Save progress for all event-based hearts
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

    /**
     * Loads all heart progress data from their respective files.
     * 
     * This should be called during plugin startup to restore
     * player progress from previous sessions. Safe to call even
     * if some listeners aren't initialized or files don't exist.
     */
    fun loadAll() {
        // Load progress for all event-based hearts
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
