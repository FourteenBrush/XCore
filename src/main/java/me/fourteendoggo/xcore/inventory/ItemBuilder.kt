package me.fourteendoggo.xcore.inventory

import me.fourteendoggo.xcore.utils.Utils
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

class ItemBuilder(private val itemStack: ItemStack) {
    private val itemMeta = itemStack.itemMeta

    // CraftItemMeta does some nonsense while setting lore, so we keep track of it ourselves, until
    // building the final ItemStack
    private var lore: MutableList<String> = ArrayList()

    @JvmOverloads
    constructor(material: Material, amount: Int = 1) : this(ItemStack(material, amount))

    fun displayName(name: String?): ItemBuilder {
        itemMeta?.setDisplayName(Utils.colorizeWithHex(name))
        return this
    }

    fun addEnchant(enchantment: Enchantment, level: Int): ItemBuilder {
        itemMeta?.addEnchant(enchantment, level, true)
        return this
    }

    fun lore(lore: List<String>): ItemBuilder {
        val coloredLore = ArrayList<String>()
        for (line in lore) {
            coloredLore.add(Utils.colorizeWithHex(line))
        }
        this.lore = coloredLore
        return this
    }

    fun lore(vararg lore: String): ItemBuilder {
        val coloredLore = ArrayList<String>(lore.size)
        for (line in lore) {
            coloredLore.add(Utils.colorizeWithHex(line))
        }
        this.lore = coloredLore
        return this
    }

    fun addLore(vararg lore: String?): ItemBuilder {
        for (line in lore) {
            this.lore.add(Utils.colorizeWithHex(line))
        }
        return this
    }

    fun build(): ItemStack {
        itemMeta?.lore = lore
        itemStack.setItemMeta(itemMeta)
        return itemStack
    }
}
