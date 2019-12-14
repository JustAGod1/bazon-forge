package ru.justagod.bazon.data

import ru.justagod.model.ClassTypeReference
import ru.justagod.bazon.stackManipulation.*
import org.objectweb.asm.Opcodes
import java.io.DataOutput

class DataOutputFlowWriter(val variable: () -> Int) : ru.justagod.bazon.data.FlowWriter() {
    val type = ClassTypeReference(DataOutput::class.java.name)

    private fun loadOutput(appender: BytecodeAppender) {
        appender += LoadVariableInstruction(type, variable)
    }

    override fun writeInt(appender: BytecodeAppender, value: ru.justagod.bazon.stackManipulation.InstructionSet) {
        loadOutput(appender)
        appender += value
        appender += InvokeInstanceMethodInstruction(type, "writeInt", "(I)V", Opcodes.INVOKEINTERFACE)
    }

    override fun writeLong(appender: BytecodeAppender, value: ru.justagod.bazon.stackManipulation.InstructionSet) {
        loadOutput(appender)
        appender += value
        appender += InvokeInstanceMethodInstruction(type, "writeLong", "(J)V", Opcodes.INVOKEINTERFACE)
    }

    override fun writeByte(appender: BytecodeAppender, value: ru.justagod.bazon.stackManipulation.InstructionSet) {
        loadOutput(appender)
        appender += value
        appender += ByteToIntInstruction
        appender += InvokeInstanceMethodInstruction(type, "writeByte", "(I)V", Opcodes.INVOKEINTERFACE)
    }

    override fun writeShort(appender: BytecodeAppender, value: ru.justagod.bazon.stackManipulation.InstructionSet) {
        loadOutput(appender)
        appender += value
        appender += ShortToIntInstruction
        appender += InvokeInstanceMethodInstruction(type, "writeShort", "(I)V", Opcodes.INVOKEINTERFACE)
    }

    override fun writeBoolean(appender: BytecodeAppender, value: ru.justagod.bazon.stackManipulation.InstructionSet) {
        loadOutput(appender)
        appender += value
        appender += InvokeInstanceMethodInstruction(type, "writeBoolean", "(Z)V", Opcodes.INVOKEINTERFACE)
    }

    override fun writeChar(appender: BytecodeAppender, value: ru.justagod.bazon.stackManipulation.InstructionSet) {
        loadOutput(appender)
        appender += value
        appender += CharToIntInstruction
        appender += InvokeInstanceMethodInstruction(type, "writeChar", "(I)V", Opcodes.INVOKEINTERFACE)
    }

    override fun writeFloat(appender: BytecodeAppender, value: ru.justagod.bazon.stackManipulation.InstructionSet) {
        loadOutput(appender)
        appender += value
        appender += InvokeInstanceMethodInstruction(type, "writeFloat", "(F)V", Opcodes.INVOKEINTERFACE)
    }

    override fun writeDouble(appender: BytecodeAppender, value: ru.justagod.bazon.stackManipulation.InstructionSet) {
        loadOutput(appender)
        appender += value
        appender += InvokeInstanceMethodInstruction(type, "writeDouble", "(D)V", Opcodes.INVOKEINTERFACE)
    }

    override fun writeString(appender: BytecodeAppender, value: ru.justagod.bazon.stackManipulation.InstructionSet) {
        loadOutput(appender)
        appender += value
        appender += InvokeInstanceMethodInstruction(type, "writeUTF", "(Ljava/lang/String;)V", Opcodes.INVOKEINTERFACE)
    }

}