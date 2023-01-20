package me.fourteendoggo.xcore.commands.vanish;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import me.fourteendoggo.xcore.user.User;
import me.fourteendoggo.xcore.utils.Lang;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.UUID;

@CommandAlias("vanish|v")
@CommandPermission("xcore.moderator")
public class VanishCommand extends BaseCommand {
    private final VanishManager vanishManager;

    public VanishCommand(VanishManager vanishManager) {
        this.vanishManager = vanishManager;
    }

    @Default
    public void onToggle(User sender) {
        vanishManager.toggle(sender);
    }

    @HelpCommand
    public void onHelp(CommandHelp help) {
        help.showHelp();
    }

    @Subcommand("enable|on")
    public void onVanishSelf(User sender) {
        if (vanishManager.vanish(sender, true)) {
            sender.sendMessage(Lang.VANISH_ENABLED);
        } else {
            sender.sendMessage(Lang.VANISH_ALREADY_VANISHED);
        }
    }

    @Subcommand("disable|off")
    public void onUnvanishSelf(User sender) {
        if (vanishManager.unvanish(sender, true, true)) {
            sender.sendMessage(Lang.VANISH_DISABLED);
        } else {
            sender.sendMessage(Lang.VANISH_ALREADY_VISIBLE);
        }
    }

    @Syntax("<player>")
    @CommandCompletion("*")
    @Subcommand("enable|on")
    private void onVanishOther(CommandSender consoleSender, User target) {
        String targetName = target.getPlayer().getName();
        if (vanishManager.vanish(target, true)) {
            consoleSender.sendMessage(Lang.VANISH_ENABLED_FOR_OTHER.asString(targetName));
        } else {
            consoleSender.sendMessage(Lang.VANISH_OTHER_ALREADY_VANISHED.asString(targetName));
        }
    }

    @Syntax("<player>")
    @CommandCompletion("*")
    @Subcommand("disable|off")
    private void onUnvanishOther(CommandSender consoleSender, User target) {
        String targetName = target.getPlayer().getName();
        if (vanishManager.unvanish(target, true, true)) {
            consoleSender.sendMessage(Lang.VANISH_DISABLED_FOR_OTHER.asString(targetName));
        } else {
            consoleSender.sendMessage(Lang.VANISH_OTHER_ALREADY_VISIBLE.asString(targetName));
        }
    }

    @Subcommand("list")
    public void onList(CommandSender consoleSender) {
        Iterator<UUID> it = vanishManager.iterator();
        if (!it.hasNext()) {
            consoleSender.sendMessage(Lang.VANISH_NOBODY_VANISHED.asString());
            return;
        }

        StringBuilder builder = new StringBuilder();
        it.forEachRemaining(uuid -> {
            Player vanishedPlayer = Bukkit.getPlayer(uuid);
            Validate.isTrue(vanishedPlayer != null && vanishedPlayer.isOnline(), "Vanished player is not online");

            if (!builder.isEmpty()) {
                builder.append(", ");
            }
            builder.append(vanishedPlayer.getName());
        });

        consoleSender.sendMessage(ChatColor.GOLD + "Vanished players: " + builder);
    }
}
