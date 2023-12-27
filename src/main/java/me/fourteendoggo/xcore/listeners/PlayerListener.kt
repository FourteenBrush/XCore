package me.fourteendoggo.xcore.listeners

import me.fourteendoggo.xcore.XCore
import me.fourteendoggo.xcore.commands.vanish.VanishManager
import me.fourteendoggo.xcore.utils.LangKey
import me.fourteendoggo.xcore.utils.Settings
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result
import org.bukkit.event.player.PlayerGameModeChangeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent

@Suppress("UNUSED")
class PlayerListener(private val core: XCore, private val vanishManager: VanishManager) : Listener {
    private val userManager = core.userManager

    @EventHandler
    fun onAsyncPreLogin(event: AsyncPlayerPreLoginEvent) {
        if (event.loginResult != Result.ALLOWED) return
        if (userManager.loadIfAbsent(event.uniqueId)) return

        event.disallow(Result.KICK_OTHER, PRE_LOGIN_FAILED_LOADING_DATA)
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val joinedPlayer = event.player
        // check if player was vanished before quitting, if so re-apply it
        val user = userManager.getUser(joinedPlayer.uniqueId)
        if (vanishManager.vanishIfFlagged(user)) {
            broadcastStaff(joinedPlayer, LangKey.JOIN_VANISHED, joinedPlayer.name)
            event.joinMessage = null
        } else {
            // normal player join
            event.joinMessage = LangKey.JOIN_MESSAGE.asString(joinedPlayer.name)
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val quitPlayer = event.player
        val user = userManager.getUser(quitPlayer.uniqueId)
        // unvanish if player was vanished, re-vanish when rejoining
        // TODO: why?
        if (vanishManager.unvanish(user, check = true, makePersistent = false)) {
            broadcastStaff(quitPlayer, LangKey.QUIT_VANISHED, quitPlayer.name)
            event.quitMessage = null
        } else {
            // normal player quit
            event.quitMessage = LangKey.QUIT_MESSAGE.asString(quitPlayer.name)
        }
    }

    /**
     * Broadcasts a message to all staff members, ignoring [ignoredPlayer], regardless of their permissions
     */
    private fun broadcastStaff(ignoredPlayer: Player, message: LangKey, vararg args: Any) {
        for (user in userManager) {
            val player = user.player
            if (player !== ignoredPlayer && player.hasPermission("xcore.moderator")) {
                user.sendMessage(message, args)
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onTeleport(event: PlayerTeleportEvent) {
        val player = event.player
        val destinationWorld = event.to?.world ?: return
        if (event.from.world != destinationWorld) return

        allowFlight(player)
    }

    @EventHandler(ignoreCancelled = true)
    fun onGameModeChange(event: PlayerGameModeChangeEvent) {
        val newGameMode = event.newGameMode
        if (newGameMode != GameMode.ADVENTURE && newGameMode != GameMode.SURVIVAL) return

        val player = event.player
        if (!vanishManager.isVanished(player)) return
        allowFlight(player)
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        val player = event.entity
        if (!vanishManager.isVanished(player)) return

        val oldDeathMessage = event.deathMessage
        event.deathMessage = null

        // when a player got killed with /kill, we need to delay setAllowFlight() call with at least 2 ticks
        // don't ask me why
        // TODO: take locale into account
        val flightDelayTicks = if (oldDeathMessage?.endsWith("fell out of the world") == true) 2 else 1
        allowFlight(player, flightDelayTicks)
    }

    private fun allowFlight(target: Player, delayTicks: Int = 1) {
        val wasFlying = target.isFlying
        Bukkit.getScheduler().runTaskLater(core, Runnable {
            target.allowFlight = true
            if (wasFlying) target.isFlying = true
        }, delayTicks.toLong())
    }

    @EventHandler(ignoreCancelled = true)
    fun onFoodLevelChange(event: FoodLevelChangeEvent) {
        val player = event.entity as Player // FIXME: unchecked cast
        val newFoodLevel = event.foodLevel

        // ignore food level changes while being vanished
        if (newFoodLevel < player.foodLevel && vanishManager.isVanished(player)) {
            event.isCancelled = true
        }
    }

    /**
     * Cancels the pickup event if:
     * - The entity picking up something is a player
     * - The settings disallow picking something up when vanished
     * - The player is actually vanished
     */
    @EventHandler(ignoreCancelled = true)
    fun onItemPickup(event: EntityPickupItemEvent) {
        if (event.entity !is Player) return
        if (Settings.pickupItemsOnVanish) return
        if (vanishManager.isVanished(event.entity as Player)) return

        event.isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun onChat(event: AsyncPlayerChatEvent) {
        // get rid of that ugly white color, like come on
        event.format = "${ChatColor.GRAY}${event.format}"
    }

    companion object {
        private const val PRE_LOGIN_FAILED_LOADING_DATA = """
            Failed to load your data, please try again in a few minutes.
            If this issue persists, contact staff.
        """
    }
}