package me.fourteendoggo.xcore.commands.vanish

import me.fourteendoggo.xcore.XCore
import me.fourteendoggo.xcore.user.User
import me.fourteendoggo.xcore.utils.LangKey
import me.fourteendoggo.xcore.utils.Reloadable
import me.fourteendoggo.xcore.utils.Settings
import me.fourteendoggo.xcore.utils.Utils
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.NamespacedKey
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

class VanishManager(private val core: XCore) : Iterable<UUID>, Reloadable {
    private val userManager = core.userManager
    private val vanishedPlayers = HashSet<UUID>()
    private val persistentKey: NamespacedKey = NamespacedKey(core, "Vanished")
    private val bossBar: BossBar = Bukkit.createBossBar("Vanished", Settings.bossBarColor, BarStyle.SOLID)

    override fun iterator() = vanishedPlayers.iterator()

    override fun reload() {
        bossBar.color = Settings.bossBarColor
    }

    fun destroyState() = bossBar.removeAll()

    fun isVanished(target: Player) = vanishedPlayers.contains(target.uniqueId)

    fun toggle(target: User) {
        if (unvanish(target, check = true, makePersistent = true)) {
            target.sendMessage(LangKey.VANISH_DISABLED)
        } else {
            vanish(target, check = false)
            target.sendMessage(LangKey.VANISH_ENABLED)
        }
    }

    fun vanishIfFlagged(target: User): Boolean {
        val pdc = target.player.persistentDataContainer
        val flag = pdc.getOrDefault(persistentKey, PersistentDataType.BYTE, -1)

        return when (isVanishFlag(flag)) {
            true -> vanish(target, check = true, broadcastStaff = false, flag)
            false -> true
        }
    }

    fun vanish(target: User, check: Boolean): Boolean {
        val vanishFlag = when (Settings.nightVisionOnVanish) {
            true -> VANISHED_W_NIGHT_VISION
            false -> VANISHED_WO_NIGHT_VISION
        }

        return vanish(target, check, broadcastStaff = true, vanishFlag)
    }

    private fun vanish(target: User, check: Boolean, broadcastStaff: Boolean, vanishFlag: Byte): Boolean {
        // always put `check` last to ensure vanishedPlayers is valid
        if (!vanishedPlayers.remove(target.id) && check) return false

        val targetPlayer = target.player

        for (user in userManager) {
            val player = user.player

            if (player === targetPlayer || !player.canSee(targetPlayer)) continue
            player.hidePlayer(core, targetPlayer)

            if (broadcastStaff && player.hasPermission(Utils.MODERATOR_PERMISSION_STRING)) {
                user.sendMessage(LangKey.VANISH_ANNOUNCE_TO_MODS, targetPlayer.name)
            }
        }
        changePlayerState(targetPlayer, true)

        if (vanishFlag == VANISHED_W_NIGHT_VISION) {
            targetPlayer.addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION, Int.MAX_VALUE, 0, false, false))
        }

        targetPlayer.persistentDataContainer.set(persistentKey, PersistentDataType.BYTE, vanishFlag)
        bossBar.addPlayer(targetPlayer)
        targetPlayer.saturation = DEFAULT_SATURATION
        targetPlayer.exhaustion = DEFAULT_EXHAUSTION
        return true
    }

    fun unvanish(target: User, check: Boolean, makePersistent: Boolean): Boolean {
        // always put `check` last to ensure vanishedPlayers is valid
        if (!vanishedPlayers.remove(target.id) && check) return false

        val targetPlayer = target.player

        for (user in userManager) {
            val player = user.player
            if (player === targetPlayer || player.canSee(targetPlayer) || !player.hasPermission(Utils.MODERATOR_PERMISSION_STRING)) continue

            player.showPlayer(core, targetPlayer)
            user.sendMessage(LangKey.VANISH_ANNOUNCE_TO_MODS, targetPlayer.name)
        }
        changePlayerState(targetPlayer, false)
        bossBar.removePlayer(targetPlayer)

        val pdc = targetPlayer.persistentDataContainer
        val vanishFlag = pdc.getOrDefault(persistentKey, PersistentDataType.BYTE, -1)

        if (vanishFlag == VANISHED_W_NIGHT_VISION) {
            targetPlayer.removePotionEffect(PotionEffectType.NIGHT_VISION)
        }
        if (makePersistent) {
            pdc.set(persistentKey, PersistentDataType.BYTE, NOT_VANISHED)
        }

        return true
    }

    private fun changePlayerState(target: Player, vanish: Boolean) {
        target.apply {
            isInvulnerable = vanish
            isSleepingIgnored = vanish

            allowFlight = vanish || gameMode == GameMode.CREATIVE || gameMode == GameMode.SPECTATOR
        }
    }

    companion object {
        /**
         * Player is not vanished but has vanished before (that's why the flag is even present)
         */
        private const val NOT_VANISHED: Byte = 0

        /**
        * Player is vanished and had night vision applied
        */
        private const val VANISHED_W_NIGHT_VISION: Byte = 1

        /**
        * Player is vanished, and we didn't give them night vision
        */
        private const val VANISHED_WO_NIGHT_VISION: Byte = 2

        fun isVanishFlag(flag: Byte) = flag == VANISHED_W_NIGHT_VISION || flag == VANISHED_WO_NIGHT_VISION

        private const val DEFAULT_SATURATION = 20f
        private const val DEFAULT_EXHAUSTION = 0f
    }
}