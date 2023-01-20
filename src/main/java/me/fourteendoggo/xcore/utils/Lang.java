package me.fourteendoggo.xcore.utils;

import me.fourteendoggo.xcore.XCore;
import org.bukkit.configuration.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public enum Lang {
    RELOADED_PLUGIN("reloaded-plugin"),
    VANISH_ENABLED("vanish.enabled.for-self"),
    VANISH_ENABLED_FOR_OTHER("vanish.enabled.for-other"),
    VANISH_ENABLED_BY_OTHER("vanish.enabled.by-other"),
    VANISH_DISABLED("vanish.disabled.for-self"),
    VANISH_DISABLED_FOR_OTHER("vanish.disabled.for-other"),
    VANISH_DISABLED_BY_OTHER("vanish.disabled.by-other"),
    VANISH_ALREADY_VANISHED("vanish.already-vanished.self"),
    VANISH_OTHER_ALREADY_VANISHED("vanish.already-vanished.other"),
    VANISH_ALREADY_VISIBLE("vanish.already-visible.self"),
    VANISH_OTHER_ALREADY_VISIBLE("vanish.already-visible.other"),
    VANISH_ANNOUNCE_TO_MODS("vanish.announce-to-mods"),
    VANISH_NOBODY_VANISHED("vanish.nobody-vanished"),
    JOIN_MESSAGE("join-message.normal"),
    JOIN_VANISHED("join-message.vanished"),
    QUIT_MESSAGE("quit-message.normal"),
    QUIT_VANISHED("quit-message.vanished"),
    HOME_LIMIT_REACHED("home.limit-reached"),
    HOME_ALREADY_EXISTS("home.already-exists"),
    HOME_NO_HOMES_CREATED("home.no-homes-created"),
    HOME_CREATED("home.created"),
    HOME_DELETED("home.deleted"),
    HOME_NOT_FOUND("home.not-found"),
    HOME_TELEPORTED("home.teleported");

    private static Config config;
    private static final Map<String, String> messages = new HashMap<>(values().length);

    private final String path;

    Lang(String path) {
        this.path = path;
    }

    public static void loadFromDisk(XCore core) {
        config = new Config(core, "lang.yml", true);
        fillMessages();
    }

    public static void reload() {
        config.reload();
        fillMessages();
    }

    private static void fillMessages() {
        boolean save = false;
        for (Lang lang : values()) {
            String message = config.getString(lang.path, null); // do not use default value
            if (message == null) {
                Configuration defaults = config.getDefaults();
                assert defaults != null;

                message = defaults.getString(lang.path);
                Objects.requireNonNull(message, "Missing default value for " + lang.path);
                config.set(lang.path, message);
                save = true;
            }
            messages.put(lang.path, Utils.colorizeWithHex(message));
        }
        if (save) {
            config.saveBlocking();
        }
    }

    public String asString() {
        return messages.get(path);
    }

    public String asString(Object... args) {
        return asString().formatted(args);
    }
}
