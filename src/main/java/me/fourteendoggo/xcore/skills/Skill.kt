package me.fourteendoggo.xcore.skills

abstract class Skill(val type: SkillType, val unlockLevel: Int = 0) {
    abstract fun getRequiredXp(level: Int): Int
}