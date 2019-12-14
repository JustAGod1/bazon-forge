package ru.justagod.bazon.utils

import ru.justagod.model.ClassTypeReference
import ru.justagod.model.InheritanceHelper
import ru.justagod.model.TypeReference

class ClassHandlerRegistry<T : Any>(private val inheritance: InheritanceHelper) {

    private val entries = arrayListOf<HandlerEntry>()

    fun findEntry(type: TypeReference): T? {
        for (serializer in entries) {
            if (!serializer.covariant && serializer.target == type) return serializer.serializer
        }
        if (type is ClassTypeReference) {
            for (entry in entries) {
                if (entry.target is ClassTypeReference && entry.covariant &&
                        try {
                            inheritance.isChild(type, entry.target)
                        } catch (e: Exception) {
                            false
                        }
                ) {
                    return entry.serializer
                }
            }
        }
        return null
    }

    fun register(type: TypeReference, serializer: T, covariant: Boolean) {
        val entry = HandlerEntry(type, covariant, serializer)
        if (entries.contains(entry)) error("The same serializer entry")
        entries += entry
    }

    private inner class HandlerEntry(
            val target: TypeReference,
            val covariant: Boolean,
            val serializer: T
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            @Suppress("UNCHECKED_CAST")
            other as ClassHandlerRegistry<*>.HandlerEntry

            if (target != other.target) return false
            if (covariant != other.covariant) return false
            if (serializer != other.serializer) return false

            return true
        }

        override fun hashCode(): Int {
            var result = target.hashCode()
            result = 31 * result + covariant.hashCode()
            result = 31 * result + (serializer?.hashCode() ?: 0)
            return result
        }
    }
}