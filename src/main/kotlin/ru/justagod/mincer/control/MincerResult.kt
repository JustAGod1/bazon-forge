package ru.justagod.mincer.control

class MincerResult(val resultedBytecode: ByteArray, val modified: Boolean) {

    fun onModification(block: (ByteArray) -> Unit) {
        if (modified) block(resultedBytecode)
    }

    infix fun merge(other: MincerResult): MincerResult {
        if (!other.modified) return this
        return other
    }
}