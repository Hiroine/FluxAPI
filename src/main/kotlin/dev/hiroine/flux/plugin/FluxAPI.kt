package dev.hiroine.flux.plugin

import dev.hiroine.flux.chat.send
import dev.hiroine.flux.kommand.kommand
import dev.hiroine.flux.ui.event.UIGlobalListener
import dev.hiroine.flux.ui.uiFx
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

class FluxAPI : JavaPlugin() {
    companion object {
        lateinit var instance: FluxAPI
    }

    override fun onEnable() {
        instance = this
        server.pluginManager.registerEvents(UIGlobalListener(), this)
    }

    override fun onDisable() {
    }
}
