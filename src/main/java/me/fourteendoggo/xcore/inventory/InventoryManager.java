package me.fourteendoggo.xcore.inventory;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class InventoryManager implements Listener {
    private final Map<Inventory, InventoryGui> inventories = new HashMap<>();

    public void registerInventory(InventoryGui gui) {
        inventories.put(gui.getInventory(), gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryGui gui = inventories.get(event.getClickedInventory());
        if (gui == null) return;

        // cancel clicks even when clicking on an empty slot
        event.setCancelled(true);

        GuiItem item = gui.getItem(event.getSlot());
        if (item == null) return;

        item.onClick(event);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        InventoryGui gui = inventories.remove(event.getInventory());
        if (gui != null) {
            gui.onClose(event);
        }
    }
}
