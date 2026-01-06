package dev.hiroine.flux.ui.model

import dev.hiroine.flux.ui.UIBuilder
import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory

class UIHolder(val builder: UIBuilder) : org.bukkit.inventory.InventoryHolder {
    override fun getInventory(): Inventory = Bukkit.createInventory(null, 9)
}