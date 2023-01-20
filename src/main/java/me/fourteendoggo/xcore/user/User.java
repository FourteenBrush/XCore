package me.fourteendoggo.xcore.user;

import me.fourteendoggo.xcore.utils.Lang;
import me.fourteendoggo.xcore.utils.LazyValue;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class User {
    private final UUID id;
    private final UserData userData;
    private final LazyValue<Player> playerGetter;

    public User(UUID id) {
        this(id, new UserData());
    }

    public User(UUID id, UserData userData) {
        this.id = id;
        this.userData = userData;
        // it's safe to hold a Player reference because when this User object is gced, the Player object will be gced too
        this.playerGetter = new LazyValue<>(() -> Bukkit.getPlayer(id), Player::isOnline);
    }

    public Player getPlayer() {
        return playerGetter.get();
    }

    public UUID getUniqueId() {
        return id;
    }

    public UserData getData() {
        return userData;
    }

    public void sendMessage(Lang message, Object... args) {
        getPlayer().sendMessage(message.asString(args));
    }
}
