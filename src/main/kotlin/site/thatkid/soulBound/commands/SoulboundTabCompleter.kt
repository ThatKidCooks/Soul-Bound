package site.thatkid.soulBound.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class SoulboundTabCompleter : TabCompleter {
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String>? {

        if (args.size == 1) {
            val normalSubcommands = listOf("help", "ability", "drain", "trust", "untrust", "trustlist")
            val adminSubcommands = listOf("help", "ability", "cooldown", "drain", "trust", "untrust", "trustlist", "add")
            if (sender.isOp) {
                return adminSubcommands.filter { it.startsWith(args[0], ignoreCase = true) }
            }
            return normalSubcommands.filter { it.startsWith(args[0], ignoreCase = true) }
        }

        if (args.size == 2 && (args[0].equals("trust", true) || args[0].equals("untrust", true))) {
            if (sender is Player) {
                return sender.server.onlinePlayers
                    .map { it.name }
                    .filter { it.startsWith(args[1], ignoreCase = true) }
            }
        }

        return emptyList()
    }
}
