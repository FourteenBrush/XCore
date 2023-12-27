package me.fourteendoggo.xcore.skills

class SkillProgress(var level: Int = 0, var xp: Int = 0) {
    operator fun component1() = level
    operator fun component2() = xp
}