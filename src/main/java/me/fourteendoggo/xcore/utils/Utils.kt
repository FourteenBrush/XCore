package me.fourteendoggo.xcore.utils

import net.md_5.bungee.api.ChatColor
import java.util.regex.Pattern

object Utils {
    private val HEX_PATTERN = Pattern.compile("#[a-fA-F0-9]{6}")
    fun colorize(input: String): String {
        return ChatColor.translateAlternateColorCodes('&', input)
    }

    fun colorizeWithHex(input: String): String {
        var input = input
        val matcher = HEX_PATTERN.matcher(input)
        while (matcher.find()) {
            val hexColor = input.substring(matcher.start(), matcher.end())
            input = input.replace(hexColor, ChatColor.of(hexColor).toString())
        }
        return colorize(input)
    }

    const val MODERATOR_PERMISSION_STRING = "xcore.moderator"
}
