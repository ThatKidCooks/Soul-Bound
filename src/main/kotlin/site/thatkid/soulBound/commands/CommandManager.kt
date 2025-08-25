package site.thatkid.soulBound.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import site.thatkid.soulBound.items.HeartRegistry
import site.thatkid.soulBound.SoulBound
import site.thatkid.soulBound.gui.admin.Hearts
import site.thatkid.soulBound.hearts.ActiveHearts
import site.thatkid.soulBound.hearts.TrustRegistry
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

/**
 * Handles all commands for the SoulBound plugin.
 * 
 * This class processes both player and administrator commands for managing hearts,
 * trust relationships, and progress tracking.
 * 
 * **Player Commands:**
 * - `/soulbound help` - Shows available commands
 * - `/soulbound ability` - Uses the special ability of their active heart
 * - `/soulbound drain` - Removes their currently active heart
 * - `/soulbound trust <player>` - Trusts a player (prevents heart effects on them)
 * - `/soulbound untrust <player>` - Removes trust from a player
 * - `/soulbound trustlist` - Lists all trusted players
 * - `/soulbound progress [heart]` - Shows progress toward obtaining hearts
 * 
 * **Admin Commands (OP only):**
 * - `/soulbound` (no args) - Opens admin GUI for managing hearts
 * - `/soulbound cooldown` - Resets all heart cooldowns
 * - `/soulbound add <player> <heart>` - Gives a specific heart to a player
 * - `/soulbound remove <player>` - Removes a player's active heart
 * - `/soulbound save` - Manually saves all plugin data
 * 
 * @param plugin Reference to the main JavaPlugin instance
 * @param soulBound Reference to the SoulBound plugin instance for access to save functionality
 */
class CommandManager(private var plugin: JavaPlugin, private var soulBound: SoulBound): CommandExecutor {
    
    /**
     * Processes all /soulbound commands and their subcommands.
     * 
     * This method routes commands to appropriate handlers based on the first argument.
     * Only players can use most commands (not console), and some commands require OP permissions.
     * 
     * @param sender The entity that executed the command
     * @param command The command that was executed
     * @param label The alias of the command that was used
     * @param args Arguments passed to the command
     * @return true if command was handled successfully, false otherwise
     */
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        // Only players can use these commands (not console)
        if (sender !is Player) {
            return true
        }

        // Handle command with no arguments - show admin GUI for OPs, help for others
        if (args.isEmpty()) {
            if (sender.isOp) {
                Hearts(plugin).open(sender)
            } else {
                sender.sendMessage("§cUsage: /soulbound help")
            }
            return true
        }

        // Route to specific command handlers based on first argument
        when (args[0].lowercase()) {
            "help" -> {
                sender.sendMessage("§aSoulbound Commands:")
                sender.sendMessage("§e/soulbound ability - Use your heart's special ability.")
                sender.sendMessage("§e/soulbound drain - Drains your heart.")
                sender.sendMessage("§e/soulbound cooldown - Reset heart cooldowns (OP only).")
                sender.sendMessage("§e/soulbound trust <player> - Trust a player.")
                sender.sendMessage("§e/soulbound untrust <player> - Untrust a player.")
                sender.sendMessage("§e/soulbound trustlist - List trusted players.")
            }

            // Remove player's active heart (has cooldown protection)
            "drain" -> {
                val hasCooldown = ActiveHearts.hasCooldown(sender.uniqueId)
                if (hasCooldown) {
                    sender.sendMessage("§cYou cannot drain your heart yet — cooldown active!")
                    return true
                }
                ActiveHearts.remove(sender, 1)
                sender.sendMessage("§aYour heart has been drained.")
            }

            "ability" -> {
                val hearts = ActiveHearts.getHearts(sender.uniqueId)
                if (hearts.isEmpty()) {
                    sender.sendMessage("§cYou have no active heart!")
                } else {
                    hearts.first().specialEffect(sender)
                }
            }

            "cooldown" -> {
                if (sender.isOp) {
                    Crowned.clearCooldown(sender.uniqueId)
                    Warden.clearCooldown(sender.uniqueId)
                    Ghastly.clearCooldown(sender.uniqueId)
                    Haste.clearCooldown(sender.uniqueId)
                    Trader.clearCooldown(sender.uniqueId)
                    Strength.clearCooldown(sender.uniqueId)
                    Aquatic.clearCooldown(sender.uniqueId)
                    Golem.clearCooldown(sender.uniqueId)
                    Wise.clearCooldown(sender.uniqueId)
                    Fire.clearCooldown(sender.uniqueId)
                    Wither.clearCooldown(sender.uniqueId)
                    Frozen.clearCooldown(sender.uniqueId)
                    Speed.clearCooldown(sender.uniqueId)
                    sender.sendMessage("§aYour heart cooldowns have been reset.")
                } else {
                    sender.sendMessage("§cYou don’t have permission to reset cooldowns.")
                }
            }

            // Add a player to the trust list (prevents heart abilities from affecting them)
            "trust" -> {
                if (args.size != 2) {
                    sender.sendMessage("§cUsage: /soulbound trust <player>")
                } else {
                    val target = plugin.server.getPlayer(args[1])
                    if (target == null) {
                        sender.sendMessage("§cCould not find player '${args[1]}'.")
                    } else {
                        TrustRegistry.trust(sender.uniqueId, target.uniqueId)
                        sender.sendMessage("§aYou have trusted ${target.name}.")
                    }
                }
            }

            // Remove a player from the trust list
            "untrust" -> {
                if (args.size != 2) {
                    sender.sendMessage("§cUsage: /soulbound untrust <player>")
                } else {
                    val target = plugin.server.getPlayer(args[1])
                    if (target == null) {
                        sender.sendMessage("§cCould not find player '${args[1]}'.")
                    } else {
                        TrustRegistry.untrust(sender.uniqueId, target.uniqueId)
                        sender.sendMessage("§aYou have untrusted ${target.name}.")
                    }
                }
            }

            // Display list of all trusted players
            "trustlist" -> {
                val trusted = TrustRegistry.getTrusted(sender.uniqueId)

                if (trusted.isEmpty()) {
                    sender.sendMessage("§7You haven't trusted anyone yet.")
                } else {
                    val names = trusted.mapNotNull { plugin.server.getOfflinePlayer(it) }
                    sender.sendMessage("§aTrusted Players: §f${names.joinToString(", ") { it.name ?: it.uniqueId.toString() }}")
                }
            }

            "add" -> {
                if (sender.isOp) {
                    if (args.size < 3) {
                        sender.sendMessage("§cUsage: /soulbound add <player> <heart>")
                        return true
                    }

                    val player = plugin.server.getPlayer(args[1])

                    val heartName = args[2].lowercase()
                    val heart = when (heartName) {
                        "crowned" -> Crowned
                        "warden" -> Warden
                        "ghastly" -> Ghastly
                        "haste" -> Haste
                        "trader" -> Trader
                        "strength" -> Strength
                        "aquatic" -> Aquatic
                        "golem" -> Golem
                        "wise" -> Wise
                        "fire" -> Fire
                        "wither" -> Wither
                        else -> null
                    }

                    if (heart != null) {
                        ActiveHearts.add(player?.uniqueId ?: sender.uniqueId, heart)
                        sender.sendMessage("§aYou have added the ${heartName.capitalize()} heart.")
                    } else {
                        sender.sendMessage("§cUnknown heart type: $heartName")
                    }
                } else {
                    sender.sendMessage("§cUnknown subcommand. Use /soulbound help for help.")
                }
            }

            "remove" -> {
                if (sender.isOp) {
                    if (args.size < 2) {
                        sender.sendMessage("§cUsage: /soulbound remove <player>")
                        return true
                    }

                    val heartName = ActiveHearts.remove(sender, 1)
                    sender.sendMessage("§aYou have removed the $heartName heart.")
                    sender.sendMessage("§cUnknown heart type: $heartName")
                } else {
                    sender.sendMessage("§cYou don’t have permission to use this command.")
                }
            }

            "progress" -> {
                if (args.size < 2) {
                    sender.sendMessage("§a=== Heart Progress ===")
                    HeartRegistry.hearts.values.forEach { heart ->
                        sender.sendMessage(heart.checkProgress(sender))
                    }
                    return true
                }

                val target = args[1].lowercase()
                if (target == "all") {
                    sender.sendMessage("§a=== Heart Progress ===")
                    HeartRegistry.hearts.values.forEach { heart ->
                        sender.sendMessage(heart.checkProgress(sender))
                    }
                    return true
                }

                val heart = HeartRegistry.hearts[target]
                if (heart == null) {
                    sender.sendMessage("§cUnknown heart type: $target")
                    return true
                }

                sender.sendMessage(heart.checkProgress(sender))
                return true
            }

            "save" -> {
                if (sender.isOp) {
                    soulBound.save()
                    sender.sendMessage("§aHearts saved successfully.")
                    return true
                } else {
                    sender.sendMessage("§cYou don’t have permission to save hearts.")
                    return true
                }
            }

            "" -> {

            }

            else -> {
                sender.sendMessage("§cUnknown subcommand. Use /soulbound help for help.")
            }
        }
        return false
    }
}
