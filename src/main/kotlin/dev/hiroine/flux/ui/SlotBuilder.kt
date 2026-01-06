package dev.hiroine.flux.ui

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class SlotBuilder(val item: ItemStack) {
    var clickHandler: (InventoryClickEvent.() -> Unit)? = null
    fun onClick(action: InventoryClickEvent.() -> Unit) { this.clickHandler = action }
}