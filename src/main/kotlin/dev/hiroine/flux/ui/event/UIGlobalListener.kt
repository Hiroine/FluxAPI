package dev.hiroine.flux.ui.event

import dev.hiroine.flux.ui.model.UIHolder
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent

class UIGlobalListener : Listener {
    @EventHandler
    fun onInvClick(e: InventoryClickEvent) {
        val holder = e.inventory.holder as? UIHolder ?: return
        e.isCancelled = true
        holder.builder.slots[e.rawSlot]?.clickHandler?.invoke(e)
    }

    @EventHandler
    fun onInvOpen(e: InventoryOpenEvent) {
        (e.inventory.holder as? UIHolder)?.builder?.openHandler?.invoke(e)
    }

    @EventHandler
    fun onInvClose(e: InventoryCloseEvent) {
        (e.inventory.holder as? UIHolder)?.builder?.closeHandler?.invoke(e)
    }
}