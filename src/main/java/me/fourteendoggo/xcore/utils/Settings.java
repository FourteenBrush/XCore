package me.fourteendoggo.xcore.utils;

import me.fourteendoggo.xcore.XCore;
import org.bukkit.boss.BarColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class Settings {
    private static Config config;

    public static void loadFromDisk(XCore core) {
        config = new Config(core, "config.yml", true);
    }

    public static void reload() {
        config.reload();
    }

    public static String getMongoHost() {
        return Property.MONGO_HOST.asString();
    }

    public static BarColor getBossBarColor() {
        return Property.VANISH_BOSS_BAR_COLOR.asBarColor();
    }

    public static boolean getNightVisionOnVanish() {
        return Property.VANISH_APPLY_NIGHT_VISION.asBoolean();
    }

    public static boolean getPickupItemsOnVanish() {
        return Property.VANISH_PICKUP_ITEMS.asBoolean();
    }

    public static int getHomesLimit(Player target) {
        ConfigurationSection section = config.getConfigurationSection("home-limits");
        assert section != null;
        for (String permission : section.getKeys(false)) {
            if (target.hasPermission("xcore." + permission)) {
                return config.getInt("home-limits." + permission);
            }
        }
        return config.getInt("home-limits.default", 3);
    }

    private enum Property {
        MONGO_HOST("mongodb-host"),
        VANISH_BOSS_BAR_COLOR("vanish.boss-bar-color"),
        VANISH_APPLY_NIGHT_VISION("vanish.apply-night-vision"),
        VANISH_PICKUP_ITEMS("vanish.pickup-items"),
        ;

        private final String path;

        Property(String path) {
            this.path = path;
        }

        String asString() {
            return config.getString(path);
        }

        int asInt() {
            return config.getInt(path);
        }

        boolean asBoolean() {
            return config.getBoolean(path);
        }

        BarColor asBarColor() {
            return switch (asString().toLowerCase()) {
                case "pink" -> BarColor.PINK;
                case "blue" -> BarColor.BLUE;
                case "red" -> BarColor.RED;
                case "green" -> BarColor.GREEN;
                case "yellow" -> BarColor.YELLOW;
                case "white" -> BarColor.WHITE;
                default -> BarColor.PURPLE;
            };
        }
    }
}
