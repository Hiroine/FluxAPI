package dev.hiroine.flux.tag

import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin

fun <T : Any> PersistentDataHolder.setTag(plugin: Plugin, key: String, value: T) {
    val namespacedKey = NamespacedKey(plugin, key)

    if (value is Location) {
        this.persistentDataContainer.set(namespacedKey, PersistentDataType.STRING, TAG.locationToString(value))
        return
    }

    val type = TAG.getDataType(value::class.java) as? PersistentDataType<Any, T> ?: return
    this.persistentDataContainer.set(namespacedKey, type, value)
}

inline fun <reified T : Any> PersistentDataHolder.getTag(plugin: Plugin, key: String): T? {
    val namespacedKey = NamespacedKey(plugin, key)

    if (T::class.java.isAssignableFrom(Location::class.java)) {
        val data = this.persistentDataContainer.get(namespacedKey, PersistentDataType.STRING) ?: return null
        return TAG.stringToLocation(data) as? T
    }

    val type = TAG.getDataType(T::class.java) as? PersistentDataType<out Any, T> ?: return null
    return this.persistentDataContainer.get(namespacedKey, type)
}


fun PersistentDataHolder.hasTag(plugin: Plugin, key: String): Boolean {
    return this.persistentDataContainer.has(NamespacedKey(plugin, key))
}


fun PersistentDataHolder.removeTag(plugin: Plugin, key: String) {
    this.persistentDataContainer.remove(NamespacedKey(plugin, key))
}

inline fun <reified T : Any> ItemStack.getTag(plugin: Plugin, key: String): T? {
    return this.itemMeta?.getTag<T>(plugin, key)
}

fun <T : Any> ItemStack.setTag(plugin: Plugin, key: String, value: T) {
    val meta = this.itemMeta ?: return
    meta.setTag(plugin, key, value)
    this.itemMeta = meta // 중요: 수정한 메타를 다시 아이템에 넣어야 저장됨
}

fun ItemStack.hasTag(plugin: Plugin, key: String): Boolean {
    return this.itemMeta?.hasTag(plugin, key) ?: false
}

fun ItemStack.removeTag(plugin: Plugin, key: String) {
    val meta = this.itemMeta ?: return
    meta.removeTag(plugin, key)
    this.itemMeta = meta
}