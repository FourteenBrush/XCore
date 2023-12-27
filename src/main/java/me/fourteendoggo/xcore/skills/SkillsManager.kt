package me.fourteendoggo.xcore.skills

import me.fourteendoggo.xcore.user.User

class SkillsManager {
    private val skills = HashMap<SkillType, Skill>()

    fun addSkill(skill: Skill) = skills.put(skill.type, skill)

    fun addSkills(vararg skills: Skill) = skills.forEach(::addSkill)

    fun removeSkill(type: SkillType) = skills.remove(type)

    fun handleLeveling(type: SkillType, user: User) {
        val progress = user.data.getSkillProgress(type) ?: return
        val skill = skills[type] ?: return
        val (currentLevel, currentXp) = progress
        val requiredXp = skill.getRequiredXp(currentLevel)

        if (currentXp >= requiredXp) {
            progress.level++
            progress.xp -= requiredXp
            user.levelUpSkill(type, currentLevel + 1)
        } else {
            user.showSkillProgress(type, currentXp, requiredXp)
        }
    }
}