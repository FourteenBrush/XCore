package me.fourteendoggo.xcore.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description
import me.fourteendoggo.xcore.XCore
import me.fourteendoggo.xcore.inventory.GuiItem
import me.fourteendoggo.xcore.inventory.InventoryGui
import me.fourteendoggo.xcore.inventory.ItemBuilder
import me.fourteendoggo.xcore.user.Home
import me.fourteendoggo.xcore.user.User
import me.fourteendoggo.xcore.utils.LangKey
import me.fourteendoggo.xcore.utils.Settings
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent

class HomeCommand(private val core: XCore) : BaseCommand() {
    @CommandAlias("sethome")
    @Description("Sets a home at your current location")
    fun onSetHome(sender: User, name: String) {
        val player = sender.player
        val homesLimit = Settings.getHomesLimit(player)

        if (sender.data.homesAmount >= homesLimit) {
            sender.sendMessage(LangKey.HOME_LIMIT_REACHED)
            return
        }
        if (!sender.data.addHome(name, player)) {
            sender.sendMessage(LangKey.HOME_ALREADY_EXISTS)
            return
        }

        sender.sendMessage(LangKey.HOME_CREATED, name)
    }

    @CommandAlias("delhome")
    @Description("Deletes the home with the given name")
    fun onDeleteHome(sender: User, home: Home?) {
        if (home == null) {
            sender.sendMessage(LangKey.HOME_NOT_FOUND)
        } else {
            val homeDeletion = core.storage.deleteHome(home)
            homeDeletion.thenRun { sender.sendMessage(LangKey.HOME_DELETED) }
        }
    }

    @CommandAlias("homes")
    @Description("Shows all your homes")
    fun onHomeList(sender: User) {
        val userData = sender.data

        if (userData.homesAmount == 0) {
            sender.sendMessage(LangKey.HOME_NO_HOMES_CREATED)
            return
        }

        val gui = InventoryGui("#a4790Homes", 5)
        core.inventoryManager.registerInventory(gui)

        for ((slot, home) in userData.homes.withIndex()) {
            var material = Material.DIAMOND
            var onClick = { _: InventoryClickEvent -> }
            val lore = home.location.run {
                mutableListOf("&6x: $blockX, y: $blockY, z: $blockZ")
            }

            if (home.location.isWorldLoaded) {
                lore += "&7Click here to teleport to this home"
                onClick = {
                    sender.player.teleport(home.location)
                    sender.sendMessage(LangKey.HOME_TELEPORTED, home.name)
                }
            } else {
                material = Material.BARRIER
                lore += "&cCannot teleport to a home in an unloaded world"
            }

            val item = ItemBuilder(material)
                .displayName("${ChatColor.GOLD}${home.name}")
                .lore(lore)
                .build()

            gui[slot] = GuiItem(item, onClick)
        }
    }
}