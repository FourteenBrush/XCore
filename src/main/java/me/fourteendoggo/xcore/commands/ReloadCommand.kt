package me.fourteendoggo.xcore.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import me.fourteendoggo.xcore.XCore
import me.fourteendoggo.xcore.user.sendMessage
import me.fourteendoggo.xcore.utils.Lang
import org.bukkit.command.CommandSender

@CommandAlias("reload")
@CommandPermission("xcore.admin")
class ReloadCommand(private val core: XCore) : BaseCommand() {
    @Suppress("UNUSED")
    @Default
    fun onReload(sender: CommandSender) {
        core.reload()
        sender.sendMessage(Lang.RELOADED_PLUGIN)
    }
}
