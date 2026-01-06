package dev.hiroine.flux.kommand.model

data class ArgumentDefinition(
    val name: String,
    val type: ArgumentType,
    val optional: Boolean = false
)
