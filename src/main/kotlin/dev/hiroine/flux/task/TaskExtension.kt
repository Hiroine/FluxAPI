package dev.hiroine.flux.task

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask

fun Plugin.task(action: () -> Unit): BukkitTask {
    return Bukkit.getScheduler().runTask(this, Runnable { action() })
}

fun Plugin.taskLater(delay: Long, action: () -> Unit): BukkitTask {
    return Bukkit.getScheduler().runTaskLater(this, Runnable { action() }, delay)
}

fun Plugin.taskTimer(delay: Long, period: Long, action: (BukkitTask) -> Unit): BukkitTask {
    lateinit var task: BukkitTask
    task = Bukkit.getScheduler().runTaskTimer(this, Runnable { action(task) }, delay, period)
    return task
}