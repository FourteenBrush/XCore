package me.fourteendoggo.xcore.user

import me.fourteendoggo.xcore.skills.SkillProgress
import me.fourteendoggo.xcore.skills.SkillType
import org.bukkit.entity.Player
import org.jetbrains.annotations.UnmodifiableView
import java.util.Collections
import java.util.EnumMap

class UserData(var level: Int = 0) {
    private val _homes = HashMap<String, Home>()
    private val _skills = EnumMap<SkillType, SkillProgress>(SkillType::class.java)

    val homes: @UnmodifiableView Collection<Home>
        get() = Collections.unmodifiableCollection(_homes.values)
    val homesAmount: Int
        get() = _homes.size
    val skills: @UnmodifiableView Map<SkillType, SkillProgress>
        get() = Collections.unmodifiableMap(_skills)

    fun removeHome(name: String) = _homes.remove(name)

    fun addHome(home: Home) {
        _homes[home.name] = home
    }

    fun addHome(name: String, player: Player): Boolean {
        val oldSize = homes.size
        _homes.computeIfAbsent(name) { Home(name, player.uniqueId, player.location) }
        return homes.size > oldSize
    }

    fun setSkillProgress(type: SkillType, progress: SkillProgress) = _skills.put(type, progress)

    fun getSkillProgress(type: SkillType) = _skills[type]

    fun incrementSkillXp(type: SkillType, amount: Int) {
        _skills.computeIfAbsent(type) { SkillProgress() }.xp += amount
    }
}