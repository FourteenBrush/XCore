package me.fourteendoggo.xcore.utils

import me.fourteendoggo.xcore.XCore
import org.bukkit.boss.BarColor
import org.bukkit.entity.Player
import java.util.*

object Settings : Reloadable {
    private lateinit var config: Config
    fun loadFromDisk(core: XCore) {
        config = Config(core, "config.yml", true)
    }

    override fun reload() = config.reload()

    val mongoHost: String
        get() = Property.MONGO_HOST.asString()!!
    val bossBarColor: BarColor
        get() = Property.VANISH_BOSS_BAR_COLOR.asBarColor()
    val nightVisionOnVanish: Boolean
        get() = Property.VANISH_APPLY_NIGHT_VISION.asBoolean()
    val pickupItemsOnVanish: Boolean
        get() = Property.VANISH_PICKUP_ITEMS.asBoolean()

    fun getHomesLimit(target: Player): Int {
        val section = config.getConfigurationSection("home-limits")!!
        for (permission in section.getKeys(false)) {
            if (target.hasPermission("xcore.$permission")) {
                return config.getInt("home-limits.$permission")
            }
        }
        return config.getInt("home-limits.default", 3)
    }

    private enum class Property(private val path: String) {
        MONGO_HOST("mongodb-host"),
        VANISH_BOSS_BAR_COLOR("vanish.boss-bar-color"),
        VANISH_APPLY_NIGHT_VISION("vanish.apply-night-vision"),
        VANISH_PICKUP_ITEMS("vanish.pickup-items");

        fun asString() = config.getString(path)

        fun asInt() = config.getInt(path)

        fun asBoolean() = config.getBoolean(path)

        fun asBarColor() = when (asString()!!.lowercase()) {
            "pink" -> BarColor.PINK
            "blue" -> BarColor.BLUE
            "red" -> BarColor.RED
            "green" -> BarColor.GREEN
            "yellow" -> BarColor.YELLOW
            "white" -> BarColor.WHITE
            else -> BarColor.PURPLE
        }
    }
}
