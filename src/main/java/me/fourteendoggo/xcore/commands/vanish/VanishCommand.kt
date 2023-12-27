package me.fourteendoggo.xcore.commands.vanish

import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandHelp
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.HelpCommand
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import me.fourteendoggo.xcore.user.User
import me.fourteendoggo.xcore.user.sendMessage
import me.fourteendoggo.xcore.utils.Lang
import me.fourteendoggo.xcore.utils.Utils
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender

@Suppress("UNUSED")
@CommandAlias("vanish")
@CommandPermission(Utils.MODERATOR_PERMISSION_STRING)
class VanishCommand(private val vanishManager: VanishManager) : BaseCommand() {
    @Default
    fun onToggle(sender: User) = vanishManager.toggle(sender)

    @HelpCommand
    fun onHelp(help: CommandHelp) = help.showHelp()

    @Subcommand("enable|on")
    fun onVanish(sender: User) {
        when (vanishManager.vanish(sender, true)) {
            true -> sender.sendMessage(Lang.VANISH_ENABLED)
            false -> sender.sendMessage(Lang.VANISH_ALREADY_VANISHED)
        }
    }

    @Subcommand("disable|off")
    fun onUnvanish(sender: User) {
        when (vanishManager.unvanish(sender, check = true, makePersistent = true)) {
            true -> sender.sendMessage(Lang.VANISH_DISABLED)
            false -> sender.sendMessage(Lang.VANISH_ALREADY_VISIBLE)
        }
    }

    @Syntax("<player>")
    @CommandCompletion("*")
    @Subcommand("enable|on")
    fun onVanishOther(sender: CommandSender, target: User) {
        val targetName = target.player.name
        when (vanishManager.vanish(target, true)) {
            true -> sender.sendMessage(Lang.VANISH_ENABLED_FOR_OTHER, targetName)
            false -> sender.sendMessage(Lang.VANISH_OTHER_ALREADY_VANISHED, targetName)
        }
    }

    @Syntax("<player>")
    @CommandCompletion("*")
    @Subcommand("disable|off")
    fun onUnvanishOther(sender: CommandSender, target: User) {
        val targetName = target.player.name
        when (vanishManager.unvanish(target, check = true, makePersistent = true)) {
            true -> sender.sendMessage(Lang.VANISH_DISABLED_FOR_OTHER, targetName)
            false -> sender.sendMessage(Lang.VANISH_OTHER_ALREADY_VISIBLE, targetName)
        }
    }

    @Subcommand("list")
    fun onList(sender: CommandSender) {
        val vanishedPlayers = vanishManager.map { Bukkit.getPlayer(it)!! }
        if (vanishedPlayers.isEmpty()) {
            sender.sendMessage(Lang.VANISH_NOBODY_VANISHED)
            return
        }

        val sb = buildString {
            for (player in vanishedPlayers) {
                assert(player.isOnline)
                append(player.name)
                append(", ")
            }

            substring(0..length - 2)
        }

        sender.sendMessage("${ChatColor.GOLD}Vanished players: $sb")
    }
}