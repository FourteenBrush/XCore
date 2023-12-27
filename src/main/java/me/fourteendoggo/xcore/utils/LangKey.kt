package me.fourteendoggo.xcore.utils

import me.fourteendoggo.xcore.XCore
import org.jetbrains.annotations.Blocking
import java.util.*

enum class LangKey(private val path: String) {
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
    HOME_TELEPORTED("home.teleported"),
    FARMING_CROP_NOT_READY_YET("farming.crop-not-ready-yet"),
    FARMING_CANNOT_BREAK_FARMLAND("farming.cannot-break-farmland"),
    INCORRECT_TOOL("incorrect-tool");

    fun asString() = messages[path]!!

    fun asString(vararg args: Any) = String.format(asString(), args)

    companion object : Reloadable {
        private lateinit var config: Config
        private val messages = HashMap.newHashMap<String, String>(entries.size)

        @Blocking
        fun loadFromDisk(core: XCore) {
            config = Config(core, "lang.yml", true)
            fillMessages()
        }

        @Blocking // potentially
        override fun reload() {
            config.reload()
            fillMessages()
        }

        private fun fillMessages() {
            var save = false

            for (lang in entries) {
                var message = config.getString(lang.path, null) // do not use default value
                if (message == null) {
                    val defaults = config.defaults!!
                    message = defaults.getString(lang.path)
                    requireNotNull(message) { "Missing default value for ${lang.path}. This is a bug." }

                    config[lang.path] = message
                    save = true
                }
                messages[lang.path] = Utils.colorizeWithHex(message)
            }

            if (save) {
                config.saveBlocking()
            }
        }
    }
}
