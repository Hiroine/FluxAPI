package dev.hiroine.flux.kommand.model

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

class KommandContext(
    val sender: CommandSender,
    val args: Array<out String>,
    private val argDefs: List<ArgumentDefinition>
) {
    val parsedArgs = mutableMapOf<String, Any?>()

    init {
        // 현재 입력된 인자들에 한해서만 파싱 진행
        for ((index, def) in argDefs.withIndex()) {
            if (index >= args.size) break

            val raw = args[index]
            val parsed = when (def.type) {
                ArgumentType.STRING -> raw
                ArgumentType.INT -> raw.toIntOrNull()
                ArgumentType.BOOLEAN -> raw.toBooleanStrictOrNull()
                ArgumentType.PLAYER -> Bukkit.getPlayer(raw)
            }
            parsedArgs[def.name] = parsed
        }
    }

    inline fun <reified T> get(name: String): T? = parsedArgs[name] as? T
}