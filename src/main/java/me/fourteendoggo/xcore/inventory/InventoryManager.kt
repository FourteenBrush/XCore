package me.fourteendoggo.xcore.inventory

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory

@Suppress("UNUSED")
class InventoryManager : Listener {
    private val inventories = HashMap<Inventory, InventoryGui>()
    fun registerInventory(gui: InventoryGui) {
        inventories[gui.inventory] = gui
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val gui = inventories[event.clickedInventory] ?: return

        // cancel clicks even when clicking on an empty slot
        // GuiItem::onClick may override this
        event.isCancelled = true
        val item = gui[event.slot] ?: return
        item.onClick(event)
    }

    @EventHandler
    fun onClose(event: InventoryCloseEvent) {
        val gui = inventories.remove(event.inventory)
        gui?.onClose(event)
    }
}
