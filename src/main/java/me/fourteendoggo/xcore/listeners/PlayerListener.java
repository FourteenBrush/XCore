package me.fourteendoggo.xcore.listeners;

import me.fourteendoggo.xcore.XCore;
import me.fourteendoggo.xcore.commands.vanish.VanishManager;
import me.fourteendoggo.xcore.storage.UserManager;
import me.fourteendoggo.xcore.user.User;
import me.fourteendoggo.xcore.utils.Lang;
import me.fourteendoggo.xcore.utils.Settings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

public class PlayerListener implements Listener {
    private final XCore core;
    private final VanishManager vanishManager;
    private final UserManager userManager;

    public PlayerListener(XCore core, VanishManager vanishManager) {
        this.core = core;
        this.vanishManager = vanishManager;
        this.userManager = core.getUserManager();
    }

    @EventHandler
    public void onAsyncPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;

        if (!userManager.loadIfAbsent(event.getUniqueId())) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Failed to load your data, " +
                    "please try again in a few minutes.\nIf this problem continues, contact staff.");
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player joinedPlayer = event.getPlayer();
        // check if the player was vanished before quitting
        User user = userManager.getUser(joinedPlayer.getUniqueId());
        if (vanishManager.vanishIfFlagged(user)) {
            // notify staff a vanished player has been joined
            notifyStaff(joinedPlayer, Lang.JOIN_VANISHED, joinedPlayer.getName());
            event.setJoinMessage(null);
        } else {
            // normal player join
            event.setJoinMessage(Lang.JOIN_MESSAGE.asString(joinedPlayer.getName()));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player quitPlayer = event.getPlayer();
        User user = userManager.unloadUser(quitPlayer.getUniqueId());
        // un-vanish if player was vanished, re-vanish when rejoining
        if (vanishManager.unvanish(user, true, false)) {
            // notify staff a vanished player has quit
            notifyStaff(quitPlayer, Lang.QUIT_VANISHED, quitPlayer.getName());
            event.setQuitMessage(null);
        } else {
            // normal player quit
            event.setQuitMessage(Lang.QUIT_MESSAGE.asString(quitPlayer.getName()));
        }
    }

    private void notifyStaff(Player ignoredPlayer, Lang message, Object... args) {
        for (User user : userManager) {
            Player player = user.getPlayer();
            if (player != ignoredPlayer && player.hasPermission("xcore.moderator")) {
                user.sendMessage(message, args);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (event.getFrom().getWorld() != event.getTo().getWorld() && vanishManager.isVanished(player)) {
            allowFlight(player);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        GameMode newGameMode = event.getNewGameMode();
        if (newGameMode != GameMode.ADVENTURE && newGameMode != GameMode.SURVIVAL) return;

        Player player = event.getPlayer();
        if (!vanishManager.isVanished(player)) return;

        allowFlight(player);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!vanishManager.isVanished(player)) return;

        String deathMessage = event.getDeathMessage();
        event.setDeathMessage(null);

        // when a player got killed with /kill, we seem to have to delay the ::setAllowFlight call with 2 ticks
        int flightDelay = deathMessage != null && deathMessage.endsWith("fell out of the world") ? 2 : 1;
        allowFlight(player, flightDelay);
    }

    private void allowFlight(Player target) {
        allowFlight(target, 1);
    }

    private void allowFlight(Player target, int afterTicks) {
        boolean wasFlying = target.isFlying();
        Bukkit.getScheduler().runTaskLater(core, () -> {
            target.setAllowFlight(true);
            if (wasFlying) {
                target.setFlying(true);
            }
        }, afterTicks);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        Player player = (Player) event.getEntity();
        int newFoodLevel = event.getFoodLevel();
        if (newFoodLevel < player.getFoodLevel() && vanishManager.isVanished(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemPickUp(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (Settings.getPickupItemsOnVanish()) return;
        if (!vanishManager.isVanished(player)) return;

        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        event.setFormat(ChatColor.GRAY + event.getFormat()); // go away ugly white color
    }
}
