package me.fourteendoggo.xcore.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import me.fourteendoggo.xcore.XCore;
import me.fourteendoggo.xcore.inventory.GuiItem;
import me.fourteendoggo.xcore.inventory.InventoryGui;
import me.fourteendoggo.xcore.inventory.ItemBuilder;
import me.fourteendoggo.xcore.user.Home;
import me.fourteendoggo.xcore.user.User;
import me.fourteendoggo.xcore.user.UserData;
import me.fourteendoggo.xcore.utils.Lang;
import me.fourteendoggo.xcore.utils.Settings;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class HomeCommand extends BaseCommand {
    private final XCore core;

    public HomeCommand(XCore core) {
        this.core = core;
    }

    @CommandAlias("sethome")
    @Description("Sets a home at your current location")
    public void onSetHome(User sender, String name) {
        UserData userData = sender.getData();
        Player player = sender.getPlayer();
        int homesLimit = Settings.getHomesLimit(player);

        if (userData.getHomesAmount() >= homesLimit) {
            sender.sendMessage(Lang.HOME_LIMIT_REACHED);
            return;
        }
        if (!userData.addHome(new Home(name, player.getUniqueId(), player.getLocation()))) {
            sender.sendMessage(Lang.HOME_ALREADY_EXISTS);
            return;
        }
        sender.sendMessage(Lang.HOME_CREATED, name);
    }

    @CommandAlias("delhome")
    @Description("Deletes the home with the given name")
    public void onDeleteHome(User sender, Home home) {
        if (home == null) {
            sender.sendMessage(Lang.HOME_NOT_FOUND);
        } else {
            CompletableFuture<Void> homeDeletion = core.getStorage().deleteHome(home);
            homeDeletion.thenRun(() -> sender.sendMessage(Lang.HOME_DELETED));
        }
    }

    @CommandAlias("homes")
    @Description("Shows you all your homes")
    public void onHomeList(User sender) {
        UserData userData = sender.getData();

        if (userData.getHomesAmount() == 0) {
            sender.sendMessage(Lang.HOME_NO_HOMES_CREATED);
            return;
        }

        InventoryGui gui = new InventoryGui("#A4790EHomes", 5);

        int slot = 0;
        for (Home home : userData.getHomes()) {
            Material material = Material.DIAMOND;
            Consumer<InventoryClickEvent> clickEventConsumer = null;
            List<String> lore = new ArrayList<>(Arrays.asList(
                    "&6x: %s, y: %s, z: %s".formatted(
                            home.location().getBlockX(),
                            home.location().getBlockY(),
                            home.location().getBlockZ()),
                    ""));

            if (home.location().isWorldLoaded()) {
                lore.add("&7Click to teleport to this home");
                clickEventConsumer = event -> {
                    sender.getPlayer().teleport(home.location());
                    sender.sendMessage(Lang.HOME_TELEPORTED, home.name());
                };
            } else {
                material = Material.BARRIER;
                lore.add("&cCannot teleport to a home in an unloaded world!");
            }

            ItemStack item = new ItemBuilder(material)
                    .setDisplayName(ChatColor.GOLD + home.name())
                    .setLore(lore)
                    .build();

            gui.setItem(slot++, new GuiItem(item, clickEventConsumer));
        }

        core.getInventoryManager().registerInventory(gui);
        sender.getPlayer().openInventory(gui.getInventory());
    }
}
