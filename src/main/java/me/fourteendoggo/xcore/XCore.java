package me.fourteendoggo.xcore;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.InvalidCommandArgument;
import me.fourteendoggo.xcore.commands.HomeCommand;
import me.fourteendoggo.xcore.commands.ReloadCommand;
import me.fourteendoggo.xcore.commands.vanish.VanishCommand;
import me.fourteendoggo.xcore.commands.vanish.VanishManager;
import me.fourteendoggo.xcore.inventory.InventoryManager;
import me.fourteendoggo.xcore.listeners.PlayerListener;
import me.fourteendoggo.xcore.storage.Storage;
import me.fourteendoggo.xcore.storage.UserManager;
import me.fourteendoggo.xcore.user.Home;
import me.fourteendoggo.xcore.user.User;
import me.fourteendoggo.xcore.utils.Lang;
import me.fourteendoggo.xcore.utils.Reloadable;
import me.fourteendoggo.xcore.utils.Settings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class XCore extends JavaPlugin {
    private boolean reloading;
    private Storage storage;
    private UserManager userManager;
    private VanishManager vanishManager;
    private InventoryManager inventoryManager;
    private List<Reloadable> reloadableComponents;

    @Override
    public void onLoad() {
        reloading = !Bukkit.getWorlds().isEmpty();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onEnable() {
        saveDefaultConfig();

        Settings.loadFromDisk(this);
        Lang.loadFromDisk(this);

        storage = new Storage(getLogger());
        userManager = new UserManager(storage);
        vanishManager = new VanishManager(this);
        inventoryManager = new InventoryManager();

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerListener(this, vanishManager), this);
        pm.registerEvents(inventoryManager, this);

        BukkitCommandManager commandManager = new BukkitCommandManager(this);
        commandManager.enableUnstableAPI("help");

        commandManager.getCommandContexts().registerIssuerOnlyContext(User.class, context -> {
            if (context.getSender() instanceof Player player) {
                return userManager.getUser(player.getUniqueId());
            }
            throw new InvalidCommandArgument("Console cannot execute this command");
        });

        commandManager.getCommandContexts().registerContext(Home.class, context -> {
            String name = context.popFirstArg();
            Player player = context.getPlayer();
            User user = userManager.getUser(player.getUniqueId());
            return user.getData().removeHome(name);
        });

        commandManager.registerCommand(new HomeCommand(this));
        commandManager.registerCommand(new ReloadCommand(this));
        commandManager.registerCommand(new VanishCommand(vanishManager));

        reloadableComponents = Arrays.asList(
                Lang::reload, Settings::reload, vanishManager
        );

        if (reloading && !Bukkit.getOnlinePlayers().isEmpty()) {
            getLogger().info("Reload detected, reloading all players...");
            // renew states in case of the plugins jar being replaced
            for (User user : userManager) {
                Player player = user.getPlayer();
                userManager.loadIfAbsent(player.getUniqueId());
                vanishManager.vanishIfFlagged(user);
            }
        }

        getLogger().info(reloading ? "XCore has been reloaded" : "XCore has been enabled");
    }

    @Override
    public void onDisable() {
        ifNotNull(vanishManager, VanishManager::destroyState);
        ifNotNull(userManager, UserManager::saveAllBlocking);
        ifNotNull(storage, Storage::disconnect);

        getLogger().info("XCore has been disabled");
    }

    /* avoid null pointer exceptions in onDisable due to onEnable not fully being called */
    private <T> void ifNotNull(T instance, Consumer<T> function) {
        if (instance != null) {
            function.accept(instance);
        }
    }

    public void reload() {
        reloadConfig();
        reloadableComponents.forEach(Reloadable::reload);
        getLogger().info("XCore has been reloaded");
    }

    public Storage getStorage() {
        return storage;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }
}
