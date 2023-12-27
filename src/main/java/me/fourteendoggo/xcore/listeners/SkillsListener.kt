package me.fourteendoggo.xcore.listeners

import me.fourteendoggo.xcore.skills.SkillType
import me.fourteendoggo.xcore.skills.SkillsManager
import me.fourteendoggo.xcore.storage.UserManager
import me.fourteendoggo.xcore.user.sendMessage
import me.fourteendoggo.xcore.utils.LangKey
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.data.Ageable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent

@Suppress("UNUSED")
class SkillsListener(private val userManager: UserManager, private val skillsManager: SkillsManager) : Listener {
    @EventHandler(ignoreCancelled = true)
    fun onCropTrample(event: PlayerInteractEvent) {
        if (event.action != Action.PHYSICAL) return
        val clickedBlock = event.clickedBlock ?: return
        if (clickedBlock.type != Material.FARMLAND) return

        event.isCancelled = true
        event.player.sendMessage(LangKey.FARMING_CANNOT_BREAK_FARMLAND)
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player
        if (player.gameMode == GameMode.CREATIVE) return
        val material = event.block.type

        fun handleProgress(type: SkillType, toolPredicate: (Material) -> Boolean) {
            val mainHandItem = player.inventory.itemInMainHand
            if (mainHandItem.type.isAir) return

            if (!toolPredicate(mainHandItem.type)) {
                player.sendMessage(LangKey.INCORRECT_TOOL)
                event.isCancelled = true
                return
            }

            val user = userManager[player.uniqueId]
            user.data.incrementSkillXp(type, 1) // TODO: change to proper values accordingly to each material
            skillsManager.handleLeveling(type, user)
        }

        when {
            Tag.CROPS.isTagged(material) -> {
                val crop = event.block.blockData as Ageable
                if (crop.age < crop.maximumAge) {
                    player.sendMessage(LangKey.FARMING_CROP_NOT_READY_YET, crop.age, crop.maximumAge)
                    event.isCancelled = true
                    return
                }

                handleProgress(SkillType.Farming, HOE_MATERIALS::contains)
            }
            Tag.LOGS_THAT_BURN.isTagged(material) -> {
                handleProgress(SkillType.Woodcutting, AXE_MATERIALS::contains)
            }
            Tag.STONE_ORE_REPLACEABLES.isTagged(material) -> {
                handleProgress(SkillType.Mining, PICKAXE_MATERIALS::contains)
            }
        }
    }

    companion object {
        private val HOE_MATERIALS = arrayOf(
            Material.WOODEN_HOE, Material.STONE_HOE, Material.IRON_HOE,
            Material.GOLDEN_HOE, Material.DIAMOND_HOE, Material.NETHERITE_HOE,
        )

        private val AXE_MATERIALS = arrayOf(
            Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE,
            Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE,
        )

        private val PICKAXE_MATERIALS = arrayOf(
            Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE,
            Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE,
        )
    }
}