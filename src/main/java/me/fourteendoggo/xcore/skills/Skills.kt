package me.fourteendoggo.xcore.skills

// Stateless skill declarations

object FarmingSkill : Skill(SkillType.Farming) {
    override fun getRequiredXp(level: Int) = 5 + level
}

object WoodCuttingSkill : Skill(SkillType.Woodcutting) {
    override fun getRequiredXp(level: Int) = 5 + level
}

object MiningSkill : Skill(SkillType.Mining) {
    override fun getRequiredXp(level: Int) = 5 + level
}