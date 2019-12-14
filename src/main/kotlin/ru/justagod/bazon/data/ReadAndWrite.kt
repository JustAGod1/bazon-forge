package ru.justagod.bazon.data

import ru.justagod.bazon.stackManipulation.BytecodeAppender
import ru.justagod.bazon.stackManipulation.InstructionSet


sealed class Writer {
    abstract fun writeInt(appender: BytecodeAppender, value: ru.justagod.bazon.stackManipulation.InstructionSet)

    abstract fun writeLong(appender: BytecodeAppender, value: ru.justagod.bazon.stackManipulation.InstructionSet)

    abstract fun writeByte(appender: BytecodeAppender, value: ru.justagod.bazon.stackManipulation.InstructionSet)

    abstract fun writeShort(appender: BytecodeAppender, value: ru.justagod.bazon.stackManipulation.InstructionSet)

    abstract fun writeBoolean(appender: BytecodeAppender, value: ru.justagod.bazon.stackManipulation.InstructionSet)

    abstract fun writeChar(appender: BytecodeAppender, value: ru.justagod.bazon.stackManipulation.InstructionSet)

    abstract fun writeString(appender: BytecodeAppender, value: ru.justagod.bazon.stackManipulation.InstructionSet)

    abstract fun writeFloat(appender: BytecodeAppender, value: ru.justagod.bazon.stackManipulation.InstructionSet)

    abstract fun writeDouble(appender: BytecodeAppender, value: ru.justagod.bazon.stackManipulation.InstructionSet)
}

abstract class FlowWriter : ru.justagod.bazon.data.Writer()

abstract class TagWriter : ru.justagod.bazon.data.Writer() {

    abstract fun startArray()

    abstract fun startObject()

    abstract fun endArray()

    abstract fun endObject()

    abstract fun name(value: String)
}

sealed class Reader {
    abstract fun readInt(appender: BytecodeAppender)

    abstract fun readLong(appender: BytecodeAppender)

    abstract fun readByte(appender: BytecodeAppender)

    abstract fun readShort(appender: BytecodeAppender)

    abstract fun readBoolean(appender: BytecodeAppender)

    abstract fun readChar(appender: BytecodeAppender)

    abstract fun readString(appender: BytecodeAppender)

    abstract fun readFloat(appender: BytecodeAppender)

    abstract fun readDouble(appender: BytecodeAppender)
}

abstract class FlowReader : ru.justagod.bazon.data.Reader()

abstract class TagReader : ru.justagod.bazon.data.Reader() {

    abstract fun startArray()

    abstract fun startObject()

    abstract fun endArray()

    abstract fun endObject()

    abstract fun arraySize(): ru.justagod.bazon.stackManipulation.InstructionSet

    abstract fun containsTag(name: String): ru.justagod.bazon.stackManipulation.InstructionSet

    abstract fun name(value: String)
}