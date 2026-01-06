package dev.hiroine.flux.item

import dev.hiroine.flux.chat.colorize
import dev.hiroine.flux.item.model.LoreFormat
import dev.hiroine.flux.tag.TAG
import dev.hiroine.flux.tag.getTag
import dev.hiroine.flux.tag.setTag
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin

class ItemBuilder(private val item: ItemStack, private val plugin: Plugin) {
    private val meta: ItemMeta = item.itemMeta ?: throw IllegalArgumentException("ItemMeta missing")

    // 로어 관리를 위한 구조
    private val loreFormats = mutableMapOf<String, LoreFormat>()
    private val manualLines = mutableMapOf<Int, MutableList<String>>()

    init {
        loadData()
    }

    private fun loadData() {
        meta.persistentDataContainer.keys.forEach { namespacedKey ->
            val key = namespacedKey.key

            // 1. 포맷 복구
            if (key.startsWith("format_")) {
                val dataKey = key.removePrefix("format_")
                val format = meta.getTag<String>(plugin, key) ?: ""
                val section = meta.getTag<Int>(plugin, "section_$dataKey") ?: 1
                loreFormats[dataKey] = LoreFormat(section, format)
            }

            // 2. [해결] 기존 수동 라인(Line) 복구
            // "line_{section}_{index}" 형태로 저장된 데이터를 찾아 manualLines를 채웁니다.
            if (key.startsWith("line_")) {
                val parts = key.split("_") // line, section, index
                if (parts.size >= 3) {
                    val section = parts[1].toIntOrNull() ?: 1
                    val text = meta.getTag<String>(plugin, key) ?: ""
                    manualLines.getOrPut(section) { mutableListOf() }.add(text)
                }
            }
        }
    }

    fun name(name: String) { meta.setDisplayName(name.colorize()) }

    fun amount(count: Int) {
        item.amount = count.coerceIn(1, 64) // 1~64개 사이로 제한
    }

    fun durability(value: Int) {
        if (meta is org.bukkit.inventory.meta.Damageable) {
            val maxDurability = item.type.maxDurability.toInt()
            val damage = (maxDurability - value).coerceAtLeast(0)
            (meta as org.bukkit.inventory.meta.Damageable).damage = damage
        }
    }

    fun damage(damageValue: Int) {
        if (meta is org.bukkit.inventory.meta.Damageable) {
            meta.damage = damageValue
        }
    }

    /** [문제 1 해결] 데이터를 저장함과 동시에 즉시 로어 포맷 정보를 갱신 */
    fun <T : Any> data(section: Int = 1, key: String, value: T, format: String? = null) {
        meta.setTag(plugin, key, value)

        format?.let {
            val coloredFormat = it.colorize()
            loreFormats[key] = LoreFormat(section, coloredFormat)
            meta.setTag(plugin, "format_$key", coloredFormat)
            meta.setTag(plugin, "section_$key", section)
        }
    }

    fun line(section: Int = 1, text: String) {
        val coloredText = text.colorize()
        val sectionLines = manualLines.getOrPut(section) { mutableListOf() }
        val index = sectionLines.size

        sectionLines.add(coloredText)
        // 에딧 시 복구를 위해 저장
        meta.setTag(plugin, "line_${section}_$index", coloredText)
    }

    // 바닐라 유틸리티
    fun enchant(enchant: Enchantment, level: Int) = meta.addEnchant(enchant, level, true)
    fun flag(vararg flags: ItemFlag) = meta.addItemFlags(*flags)

    /** 모든 정보를 종합하여 ItemStack 최종 완성 */
    fun sync(): ItemStack {
        val finalLore = mutableListOf<String>()
        val allSections = mutableMapOf<Int, MutableList<String>>()

        // 1. 수동 라인 결합
        manualLines.forEach { (s, lines) ->
            allSections.getOrPut(s) { mutableListOf() }.addAll(lines)
        }

        // 2. 데이터 라인 결합 (0 출력 방지를 위한 직접 꺼내기)
        loreFormats.forEach { (key, info) ->
            val namespacedKey = NamespacedKey(plugin, key)
            val container = meta.persistentDataContainer

            // [해결] has(key, type)을 사용하여 실제 저장된 데이터가 어떤 타입인지 먼저 확인합니다.
            val rawValue: Any = when {
                container.has(namespacedKey, PersistentDataType.INTEGER) ->
                    container.get(namespacedKey, PersistentDataType.INTEGER) ?: 0
                container.has(namespacedKey, PersistentDataType.DOUBLE) ->
                    container.get(namespacedKey, PersistentDataType.DOUBLE) ?: 0.0
                container.has(namespacedKey, PersistentDataType.STRING) ->
                    container.get(namespacedKey, PersistentDataType.STRING) ?: ""
                container.has(namespacedKey, TAG.UUID_TYPE) ->
                    container.get(namespacedKey, TAG.UUID_TYPE) ?: ""
                else -> "0" // 아무것도 없을 때
            }

            val formatted = info.format.replace("{value}", rawValue.toString())
            allSections.getOrPut(info.section) { mutableListOf() }.add(formatted)
        }

        // 3. 빌드 및 적용
        val sortedKeys = allSections.keys.sorted()
        sortedKeys.forEachIndexed { index, sKey ->
            finalLore.addAll(allSections[sKey]!!)
            if (index < sortedKeys.size - 1) finalLore.add("")
        }

        meta.lore = finalLore
        item.itemMeta = meta
        return item
    }

    fun build(): ItemStack {
        sync()
        return item
    }
}

fun buildItem(material: Material, plugin: Plugin, setup: ItemBuilder.() -> Unit): ItemStack {
    return ItemBuilder(ItemStack(material), plugin).apply(setup).build()
}

fun ItemStack.edit(plugin: Plugin, setup: ItemBuilder.() -> Unit): ItemStack {
    val manager = ItemBuilder(this, plugin)

    // 기존에 저장된 포맷 정보가 있다면 복구 (Reflection 생략, PDC 기반)
    this.itemMeta?.persistentDataContainer?.keys?.forEach { namespacedKey ->
        val key = namespacedKey.key
        if (key.startsWith("format_")) {
            val originalKey = key.removePrefix("format_")
            val format = this.itemMeta!!.getTag<String>(plugin, key) ?: ""
            val section = this.itemMeta!!.getTag<Int>(plugin, "section_$originalKey") ?: 1
            // 에디터 매니저에 포맷 복구
            // (이 로직은 내부적으로 manager의 loreFormats를 채우도록 설계)
        }
    }

    return manager.apply(setup).build()
}