package dev.hiroine.flux.kommand.model

data class ExecutionBlock(
    val argCount: Int,
    val action: KommandContext.() -> Unit
)