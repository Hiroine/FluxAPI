package dev.hiroine.flux.kommand

import dev.hiroine.flux.chat.send
import dev.hiroine.flux.kommand.model.ArgumentDefinition
import dev.hiroine.flux.kommand.model.ArgumentType
import dev.hiroine.flux.kommand.model.KommandContext
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class KommandBuilder(val name: String) {
    private val argDefinitions = mutableListOf<ArgumentDefinition>()
    val subCommands = mutableMapOf<String, KommandBuilder>()

    // 인자 개수별 실행 로직 저장 (Key: 인자 개수)
    private val executors = mutableMapOf<Int, KommandContext.() -> Unit>()
    var tabHandler: (KommandContext.() -> List<String>)? = null

    var isOpOnly: Boolean = false

    fun op() {
        this.isOpOnly = true
    }

    // 인자 추가
    fun argument(name: String, type: ArgumentType, optional: Boolean = false) {
        argDefinitions.add(ArgumentDefinition(name, type, optional))
    }

    // 서브 커맨드 추가
    fun sub(name: String, setup: KommandBuilder.() -> Unit) {
        subCommands[name] = KommandBuilder(name).apply(setup)
    }

    // 현재까지 선언된 argument 개수를 기준으로 실행 로직 등록
    fun execute(action: KommandContext.() -> Unit) {
        executors[argDefinitions.size] = action
    }

    // 플레이어 전용 실행 로직 등록
    fun executePlayer(action: KommandContext.(Player) -> Unit) {
        val currentCount = argDefinitions.size
        executors[currentCount] = {
            if (sender is Player) action(this, sender)
            else sender.send("&4플레이어 전용 명령어입니다.")
        }
    }

    // 인자 개수에 맞는 로직을 찾아 실행하는 핵심 함수
    fun dispatch(sender: CommandSender, args: Array<out String>) {
        if (isOpOnly && !sender.isOp) {
            sender.send("&c이 명령어를 사용할 권한이 없습니다. (OP 전용)")
            return
        }

        val action = executors[args.size]

        if (action != null) {
            val context = KommandContext(sender, args, argDefinitions.take(args.size))
            action.invoke(context)
        } else {
            // 매칭되는 인자 개수가 없을 때 에러 메시지 (또는 도움말)
            sender.send("&4잘못된 명령어입니다.")
        }
    }

    fun tab(action: KommandContext.() -> List<String>) {
        this.tabHandler = action
    }
}

class KommandBridge(private val root: KommandBuilder) : CommandExecutor, TabCompleter {
    override fun onCommand(s: CommandSender, c: Command, l: String, a: Array<out String>): Boolean {
        // 서브 커맨드 우선 처리
        if (a.isNotEmpty() && root.subCommands.containsKey(a[0])) {
            val sub = root.subCommands[a[0]]!!
            sub.dispatch(s, a.copyOfRange(1, a.size))
            return true
        }

        // 메인 커맨드 처리
        root.dispatch(s, a)
        return true
    }

    override fun onTabComplete(s: CommandSender, c: Command, l: String, a: Array<out String>): List<String>? {
        if (a.size == 1) {
            val subs = root.subCommands.keys.filter { it.startsWith(a[0], true) }
            if (subs.isNotEmpty()) return subs
        }
        return root.tabHandler?.invoke(KommandContext(s, a, emptyList()))
    }
}

fun kommand(name: String, block: KommandBuilder.() -> Unit) {
    val builder = KommandBuilder(name).apply(block)
    val bridge = KommandBridge(builder)
    Bukkit.getPluginCommand(name)?.let {
        it.setExecutor(bridge)
        it.tabCompleter = bridge
    }
}