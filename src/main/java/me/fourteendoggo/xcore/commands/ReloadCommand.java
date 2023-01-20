package me.fourteendoggo.xcore.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import me.fourteendoggo.xcore.XCore;
import me.fourteendoggo.xcore.utils.Lang;
import org.bukkit.command.CommandSender;

@CommandAlias("reload")
@CommandPermission("xcore.admin")
public class ReloadCommand extends BaseCommand {
    private final XCore core;

    public ReloadCommand(XCore core) {
        this.core = core;
    }

    @Default
    public void onReload(CommandSender consoleSender) {
        core.reload();
        consoleSender.sendMessage(Lang.RELOADED_PLUGIN.asString());
    }
}
