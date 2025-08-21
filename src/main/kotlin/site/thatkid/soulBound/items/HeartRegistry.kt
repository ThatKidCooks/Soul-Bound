package site.thatkid.soulBound

import org.bukkit.plugin.java.JavaPlugin
import site.thatkid.soulBound.items.Heart
import site.thatkid.soulBound.items.hearts.*
import site.thatkid.soulBound.managers.*
import site.thatkid.soulBound.managers.hearts.CrownedListener
import site.thatkid.soulBound.managers.hearts.StrengthListener
import javax.net.ssl.TrustManager

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
    lateinit var trustManager: TrustStorageManager

    fun enableAll() {
        // Initialize the listeners if they are not already initialized
        if (!this::crownedListener.isInitialized) {
            crownedListener = CrownedListener(plugin)
        }
        if (!this::strengthListener.isInitialized) {
            strengthListener = StrengthListener(plugin)
        }

        // just for crowned and strength, they are linked
        crownedListener.strengthListener = strengthListener
        strengthListener.crownedListener = crownedListener

        // enable the listeners
        crownedListener.enable()
        strengthListener.enable()
    }


    fun disableAll() {
        if (this::crownedListener.isInitialized) crownedListener.disable()
        if (this::strengthListener.isInitialized) strengthListener.disable()
    }

    fun saveAll() {
        if (this::crownedListener.isInitialized) crownedListener.save()
        if (this::strengthListener.isInitialized) strengthListener.save()
    }

    fun loadAll() {
        if (this::crownedListener.isInitialized) crownedListener.load()
        if (this::strengthListener.isInitialized) strengthListener.load()
    }
}
