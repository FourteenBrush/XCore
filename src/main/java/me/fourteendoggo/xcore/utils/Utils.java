package me.fourteendoggo.xcore.utils;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    private static final Pattern HEX_PATTERN = Pattern.compile("#[a-fA-F0-9]{6}");

    public static String colorize(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    public static String colorizeWithHex(String input) {
        Matcher matcher = HEX_PATTERN.matcher(input);
        while (matcher.find()) {
            String hexColor = input.substring(matcher.start(), matcher.end());
            input = input.replace(hexColor, ChatColor.of(hexColor).toString());
        }
        return colorize(input);
    }
}
