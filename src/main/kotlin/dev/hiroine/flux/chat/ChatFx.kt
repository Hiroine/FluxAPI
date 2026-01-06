package dev.hiroine.flux.chat

import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.regex.Pattern

private val HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})")

fun String.colorize(): String {
    var translated = this
    val matcher = HEX_PATTERN.matcher(translated)
    val buffer = StringBuffer()
    while (matcher.find()) {
        val color = matcher.group(1)
        matcher.appendReplacement(buffer, ChatColor.of("#$color").toString())
    }
    matcher.appendTail(buffer)
    return ChatColor.translateAlternateColorCodes('&', buffer.toString())
}

fun List<String>.colorize(): List<String> = this.map { it.colorize() }

fun String.stripColor(): String = ChatColor.stripColor(this)

fun CommandSender.send(message: String) {
    this.sendMessage(message.colorize())
}

fun Player.send(message: String) {
    this.sendMessage(message.colorize())
}


fun broadcast(message: String) {
    Bukkit.broadcastMessage(message.colorize())
}
