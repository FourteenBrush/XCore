package me.fourteendoggo.xcore.user

import me.fourteendoggo.xcore.skills.SkillType
import me.fourteendoggo.xcore.utils.LangKey
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.command.CommandSender
import java.util.UUID

class User(val id: UUID, val data: UserData) {
    private val skillsProgressBar = Bukkit.createBossBar("<no skill selected>", BarColor.PINK, BarStyle.SOLID)
    val player by lazy { Bukkit.getPlayer(id)!! }

    constructor(id: UUID) : this(id, UserData())

    fun invalidate() = skillsProgressBar.removeAll()

    fun sendMessage(message: LangKey, vararg args: Any) = player.sendMessage(message.asString(args))

    fun showSkillProgress(type: SkillType, currentXp: Int, requiredXp: Int) = skillsProgressBar.apply {
        setTitle("${type.displayName}: $currentXp/$requiredXp")
        addPlayer(player)
    }

    fun levelUpSkill(type: SkillType, reachedLevel: Int) = player.apply {
        playSound(location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
        playSound(location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f)
        sendMessage("${ChatColor.DARK_PURPLE}Congratulations! You reached level $reachedLevel in ${type.displayName}")
    }
}

fun CommandSender.sendMessage(message: LangKey, vararg args: Any) = sendMessage(message.asString(args))