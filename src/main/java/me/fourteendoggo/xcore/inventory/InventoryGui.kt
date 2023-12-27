package me.fourteendoggo.xcore.inventory

import me.fourteendoggo.xcore.utils.Utils
import org.bukkit.Bukkit
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory

class InventoryGui(title: String, rows: Int, private val onClose: (InventoryCloseEvent) -> Unit = {}) {
    val inventory: Inventory
    private val items = HashMap<Int, GuiItem>()

    init {
        inventory = Bukkit.createInventory(null, rows * SLOTS_PER_ROW, Utils.colorizeWithHex(title))
    }

    operator fun set(slot: Int, item: GuiItem) {
        inventory.setItem(slot, item.itemStack)
        items[slot] = item
    }

    operator fun get(slot: Int) = items[slot]

    fun onClose(event: InventoryCloseEvent) = onClose.invoke(event)

    companion object {
        const val SLOTS_PER_ROW = 9
    }
}