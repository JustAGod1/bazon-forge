package ru.justagod.serialization

import java.io.DataInput
import java.io.DataOutput

interface PersistentInator {

    fun write(i: Any, output: DataOutput)

    fun read(i: Any, output: DataInput)
}
