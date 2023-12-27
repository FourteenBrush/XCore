package me.fourteendoggo.xcore.user

import org.bukkit.entity.Player
import org.jetbrains.annotations.UnmodifiableView
import java.util.Collections

class UserData {
    private val _homes = HashMap<String, Home>()
    val homes: @UnmodifiableView Collection<Home>
        get() = Collections.unmodifiableCollection(_homes.values)
    val homesAmount: Int
        get() = _homes.size

    fun removeHome(name: String) = _homes.remove(name)

    fun addHome(home: Home) {
        _homes[home.name] = home
    }

    fun addHome(name: String, player: Player): Boolean {
        val oldSize = homes.size
        _homes.computeIfAbsent(name) { Home(name, player.uniqueId, player.location) }
        return homes.size > oldSize
    }
}