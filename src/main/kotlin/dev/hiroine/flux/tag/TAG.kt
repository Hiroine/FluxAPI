package dev.hiroine.flux.tag

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.persistence.PersistentDataType
import java.nio.ByteBuffer
import java.util.UUID

object TAG {
    val UUID_TYPE = object : PersistentDataType<ByteArray, UUID> {
        override fun getPrimitiveType(): Class<ByteArray> = ByteArray::class.java
        override fun getComplexType(): Class<UUID> = UUID::class.java

        override fun toPrimitive(complex: UUID, context: org.bukkit.persistence.PersistentDataAdapterContext): ByteArray {
            val bb = ByteBuffer.wrap(ByteArray(16))
            bb.putLong(complex.mostSignificantBits)
            bb.putLong(complex.leastSignificantBits)
            return bb.array()
        }

        override fun fromPrimitive(primitive: ByteArray, context: org.bukkit.persistence.PersistentDataAdapterContext): UUID {
            val bb = ByteBuffer.wrap(primitive)
            return UUID(bb.long, bb.long)
        }
    }

    fun locationToString(loc: Location): String {
        return "${loc.world?.name ?: "world"},${loc.x},${loc.y},${loc.z},${loc.yaw},${loc.pitch}"
    }

    fun stringToLocation(s: String): Location? {
        val p = s.split(",")
        if (p.size < 4) return null
        val world = Bukkit.getWorld(p[0]) ?: return null
        return Location(
            world,
            p[1].toDouble(), p[2].toDouble(), p[3].toDouble(),
            p.getOrNull(4)?.toFloat() ?: 0f,
            p.getOrNull(5)?.toFloat() ?: 0f
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getDataType(clazz: Class<out T>): PersistentDataType<out Any, T>? {
        // Kotlin Any에서 실제 타입을 추론하기 위해 명확하게 매핑
        return when {
            clazz.isAssignableFrom(Int::class.java) || clazz.isAssignableFrom(java.lang.Integer::class.java) -> PersistentDataType.INTEGER
            clazz.isAssignableFrom(Double::class.java) || clazz.isAssignableFrom(java.lang.Double::class.java) -> PersistentDataType.DOUBLE
            clazz.isAssignableFrom(Float::class.java) || clazz.isAssignableFrom(java.lang.Float::class.java) -> PersistentDataType.FLOAT
            clazz.isAssignableFrom(Long::class.java) || clazz.isAssignableFrom(java.lang.Long::class.java) -> PersistentDataType.LONG
            clazz.isAssignableFrom(String::class.java) -> PersistentDataType.STRING
            clazz.isAssignableFrom(UUID::class.java) -> UUID_TYPE
            clazz.isAssignableFrom(Location::class.java) -> PersistentDataType.STRING as PersistentDataType<out Any, T>
            else -> null
        } as? PersistentDataType<out Any, T>
    }
}