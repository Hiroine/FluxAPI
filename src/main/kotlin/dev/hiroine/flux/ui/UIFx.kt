package dev.hiroine.flux.ui

import dev.hiroine.flux.chat.colorize
import dev.hiroine.flux.ui.model.UIHolder
import org.bukkit.Bukkit
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class UIBuilder(val title: String, val rows: Int) {
    val slots = mutableMapOf<Int, SlotBuilder>()
    var openHandler: (InventoryOpenEvent.() -> Unit)? = null
    var closeHandler: (InventoryCloseEvent.() -> Unit)? = null
    var clickHandler: (InventoryClickEvent.() -> Unit)? = null

    fun slot(x: Int, y: Int, item: ItemStack, setup: SlotBuilder.() -> Unit = {}) {
        val slot = (y - 1) * 9 + (x - 1)
        slots[slot] = SlotBuilder(item).apply(setup)
    }

    fun onClick(action: InventoryClickEvent.() -> Unit) { this.clickHandler = action }
    fun onOpen(action: InventoryOpenEvent.() -> Unit) { this.openHandler = action }
    fun onClose(action: InventoryCloseEvent.() -> Unit) { this.closeHandler = action }

    fun build(): Inventory {
        val holder = UIHolder(this)
        val inv = Bukkit.createInventory(holder, rows * 9, title.colorize())
        slots.forEach { (index, builder) -> inv.setItem(index, builder.item) }
        return inv
    }
}

fun uiFx(title: String, rows: Int, setup: UIBuilder.() -> Unit): Inventory {
    return UIBuilder(title, rows).apply(setup).build()
}