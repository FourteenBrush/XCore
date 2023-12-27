package me.fourteendoggo.xcore.inventory

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class GuiItem(val itemStack: ItemStack, private val onClick: (InventoryClickEvent) -> Unit = {}) {
    fun onClick(event: InventoryClickEvent) = onClick.invoke(event)
}