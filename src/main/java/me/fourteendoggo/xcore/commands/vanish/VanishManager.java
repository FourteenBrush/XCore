package me.fourteendoggo.xcore.commands.vanish;

import me.fourteendoggo.xcore.XCore;
import me.fourteendoggo.xcore.storage.UserManager;
import me.fourteendoggo.xcore.user.User;
import me.fourteendoggo.xcore.utils.Lang;
import me.fourteendoggo.xcore.utils.Reloadable;
import me.fourteendoggo.xcore.utils.Settings;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public class VanishManager implements Reloadable, Iterable<UUID> {
    /**
     * Player is not vanished but has vanished before (that's why the flag is set)
     */
    private static final byte NOT_VANISHED = 0;
    /*
     * Player is vanished and the settings stated that we should give them night vision
     */
    private static final byte VANISHED_W_NIGHT_VISION = 1;
    /*
     * Player is vanished, and we didn't give them night vision
     */
    private static final byte VANISHED_WO_NIGHT_VISION = 2;

    private final XCore core;
    private final UserManager userManager;
    private final Set<UUID> vanishedPlayers;
    private final NamespacedKey persistentKey;
    private final BossBar bossBar;

    public VanishManager(XCore core) {
        this.core = core;
        this.userManager = core.getUserManager();
        this.vanishedPlayers = new HashSet<>();
        this.persistentKey = new NamespacedKey(core, "vanished");
        this.bossBar = Bukkit.createBossBar("Vanished", Settings.getBossBarColor(), BarStyle.SOLID);
    }

    @Override
    public void reload() {
        BarColor color = Settings.getBossBarColor();
        bossBar.setColor(color);
    }

    @NotNull
    @Override
    public Iterator<UUID> iterator() {
        return vanishedPlayers.iterator();
    }

    public void destroyState() {
        bossBar.removeAll();
    }

    private boolean isVanishFlag(byte flag) {
        return flag == VANISHED_W_NIGHT_VISION || flag == VANISHED_WO_NIGHT_VISION;
    }

    public boolean isVanished(Player target) {
        return vanishedPlayers.contains(target.getUniqueId());
    }

    public void toggle(User target) {
        if (unvanish(target, true, true)) {
            target.sendMessage(Lang.VANISH_DISABLED);
        } else {
            vanish(target, false);
            target.sendMessage(Lang.VANISH_ENABLED);
        }
    }

    public boolean vanishIfFlagged(User target) {
        Player player = target.getPlayer();
        PersistentDataContainer container = player.getPersistentDataContainer();
        byte flag = container.getOrDefault(persistentKey, PersistentDataType.BYTE, (byte) -1);

        if (isVanishFlag(flag)) {
            return vanish(target, true, false, flag);
        }
        return true;
    }

    public boolean vanish(User target, boolean check) {
        byte vanishFlag = Settings.getNightVisionOnVanish()
                ? VANISHED_W_NIGHT_VISION
                : VANISHED_WO_NIGHT_VISION;
        return vanish(target, check, true, vanishFlag);
    }

    public boolean vanish(User target, boolean check, boolean notifyStaff, byte vanishFlag) {
        // don't put the 'check == true' check first because vanishedPlayers would be invalid that way
        if (!vanishedPlayers.add(target.getUniqueId()) && check) {
            return false;
        }
        Player targetPlayer = target.getPlayer();

        for (User user : userManager) {
            Player player = user.getPlayer();

            if (player == targetPlayer || !player.canSee(targetPlayer)) continue;
            player.hidePlayer(core, targetPlayer);

            if (notifyStaff && !player.hasPermission("xcore.moderator")) {
                user.sendMessage(Lang.VANISH_ANNOUNCE_TO_MODS, targetPlayer.getName());
            }
        }
        changePlayerState(target.getPlayer(), true);

        if (vanishFlag == VANISHED_W_NIGHT_VISION) {
            targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
        }
        targetPlayer.getPersistentDataContainer().set(persistentKey, PersistentDataType.BYTE, vanishFlag);

        bossBar.addPlayer(targetPlayer);
        targetPlayer.setSaturation(20);
        targetPlayer.setExhaustion(0);

        return true;
    }

    public boolean unvanish(User target, boolean check, boolean makePersistent) {
        // don't put the 'check == false' check first because vanishedPlayers would be invalid that way
        if (!vanishedPlayers.remove(target.getUniqueId()) && check) {
            return false;
        }
        Player targetPlayer = target.getPlayer();

        for (User user : userManager) {
            Player player = user.getPlayer();

            if (player == targetPlayer || player.canSee(targetPlayer) || !player.hasPermission("xcore.moderator")) continue;

            player.showPlayer(core, targetPlayer);
            user.sendMessage(Lang.VANISH_ANNOUNCE_TO_MODS, targetPlayer.getName());
        }
        changePlayerState(targetPlayer, false);
        bossBar.removePlayer(targetPlayer);

        PersistentDataContainer container = targetPlayer.getPersistentDataContainer();
        byte vanishFlag = container.getOrDefault(persistentKey, PersistentDataType.BYTE, (byte) -1);

        if (vanishFlag == VANISHED_W_NIGHT_VISION) {
            targetPlayer.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }
        if (makePersistent) {
            container.set(persistentKey, PersistentDataType.BYTE, NOT_VANISHED);
        }

        return true;
    }

    private void changePlayerState(Player target, boolean vanish) {
        target.setInvulnerable(vanish);
        target.setSleepingIgnored(vanish);

        target.setAllowFlight(vanish || switch (target.getGameMode()) {
            case CREATIVE, SPECTATOR -> true;
            default -> false;
        });
    }
}
