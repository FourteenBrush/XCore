package me.fourteendoggo.xcore.user

import me.fourteendoggo.xcore.utils.Lang
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.UUID

class User(val id: UUID, val data: UserData) {
    val player by lazy { Bukkit.getPlayer(id)!! }

    constructor(id: UUID) : this(id, UserData())

    fun sendMessage(message: Lang, vararg args: Any) = player.sendMessage(message.asString(args))
}

fun CommandSender.sendMessage(message: Lang, vararg args: Any) = sendMessage(message.asString(args))