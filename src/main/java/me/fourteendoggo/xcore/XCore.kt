package me.fourteendoggo.xcore

import co.aikar.commands.BukkitCommandManager
import co.aikar.commands.InvalidCommandArgument
import me.fourteendoggo.xcore.commands.HomeCommand
import me.fourteendoggo.xcore.commands.ReloadCommand
import me.fourteendoggo.xcore.commands.vanish.VanishCommand
import me.fourteendoggo.xcore.commands.vanish.VanishManager
import me.fourteendoggo.xcore.inventory.InventoryManager
import me.fourteendoggo.xcore.listeners.PlayerListener
import me.fourteendoggo.xcore.listeners.SkillsListener
import me.fourteendoggo.xcore.skills.FarmingSkill
import me.fourteendoggo.xcore.skills.MiningSkill
import me.fourteendoggo.xcore.skills.SkillsManager
import me.fourteendoggo.xcore.skills.WoodCuttingSkill
import me.fourteendoggo.xcore.storage.Storage
import me.fourteendoggo.xcore.storage.UserManager
import me.fourteendoggo.xcore.user.Home
import me.fourteendoggo.xcore.user.User
import me.fourteendoggo.xcore.utils.LangKey
import me.fourteendoggo.xcore.utils.Reloadable
import me.fourteendoggo.xcore.utils.Settings
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class XCore : JavaPlugin() {
    private var reloading = false
    lateinit var storage: Storage private set
    lateinit var userManager: UserManager private set
    private lateinit var vanishManager: VanishManager
    lateinit var inventoryManager: InventoryManager private set
    private lateinit var reloadableComponents: Array<Reloadable>

    override fun onLoad() {
        reloading = Bukkit.getWorlds().isNotEmpty()
    }

    override fun onEnable() {
        saveDefaultConfig()

        Settings.loadFromDisk(this)
        LangKey.loadFromDisk(this)

        storage = Storage(logger)
        userManager = UserManager(storage)
        vanishManager = VanishManager(this)
        inventoryManager = InventoryManager()

        Bukkit.getPluginManager().apply {
            registerEvents(PlayerListener(this@XCore, vanishManager), this@XCore)
            registerEvents(inventoryManager, this@XCore)

            val skillsManager = SkillsManager().apply {
                addSkills(FarmingSkill, WoodCuttingSkill, MiningSkill)
            }
            registerEvents(SkillsListener(userManager, skillsManager), this@XCore)
        }

        BukkitCommandManager(this).apply {
            @Suppress("DEPRECATION")
            enableUnstableAPI("help")

            commandContexts.registerIssuerOnlyContext(User::class.java) { ctx ->
                val player = (ctx.sender as? Player) ?: throw InvalidCommandArgument("Console cannot execute this command")
                return@registerIssuerOnlyContext userManager[player.uniqueId]
            }

            commandContexts.registerContext(Home::class.java) { ctx ->
                val name = ctx.popFirstArg()
                val user = userManager[ctx.player.uniqueId]
                return@registerContext user.data.removeHome(name)
            }

            registerCommand(HomeCommand(this@XCore))
            registerCommand(ReloadCommand(this@XCore))
            registerCommand(VanishCommand(vanishManager))
        }

        reloadableComponents = arrayOf(
            LangKey.Companion, Settings, vanishManager,
        )

        if (reloading && Bukkit.getOnlinePlayers().isNotEmpty()) {
            logger.info("Reload detected, reloading all players...")
            // renew states in case of the plugins jar being replaced
            for (user in userManager) {
                // FIXME: why is this necessary?
                userManager.unloadUser(user.id)
                vanishManager.vanishIfFlagged(user)
            }
        }

        val status = if (reloading) "reloaded" else "enabled"
        logger.info("$name has been $status")
    }

    override fun onDisable() {
        vanishManager.destroyState()
        userManager.saveAllBlocking()
        storage.disconnect()

        logger.info("$name has been disabled")
    }

    fun reload() {
        reloadConfig()
        reloadableComponents.forEach(Reloadable::reload)
        logger.info("$name has been reloaded")
    }
}