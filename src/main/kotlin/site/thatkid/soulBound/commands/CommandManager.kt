package site.thatkid.soulBound.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import site.thatkid.soulBound.gui.admin.Hearts
import site.thatkid.soulBound.hearts.ActiveHearts
import site.thatkid.soulBound.hearts.TrustRegistry
import site.thatkid.soulBound.items.hearts.*

class CommandManager(private var plugin: JavaPlugin): CommandExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is Player) {
            return true
        }

        if (args.isEmpty()) {
            if (sender.isOp) {
                Hearts(plugin).open(sender)
            } else {
                sender.sendMessage("§cUsage: /soulbound help")
            }
            return true
        }

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

            "drain" -> {
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
                    sender.sendMessage("§aYour heart cooldowns have been reset.")
                } else {
                    sender.sendMessage("§cYou don’t have permission to reset cooldowns.")
                }
            }

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

            else -> {
                sender.sendMessage("§cUnknown subcommand. Use /soulbound help for help.")
            }
        }
        return true
    }
}
