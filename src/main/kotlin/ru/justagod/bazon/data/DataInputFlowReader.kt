package ru.justagod.bazon.data

import ru.justagod.model.ClassTypeReference
import ru.justagod.bazon.stackManipulation.BytecodeAppender
import ru.justagod.bazon.stackManipulation.InvokeInstanceMethodInstruction
import ru.justagod.bazon.stackManipulation.LoadVariableInstruction
import org.objectweb.asm.Opcodes
import java.io.DataInput

class DataInputFlowReader(private val variable: () -> Int) : ru.justagod.bazon.data.FlowReader() {

    private val type = ClassTypeReference(DataInput::class.java.name)

    override fun readInt(appender: BytecodeAppender) {
        appender += LoadVariableInstruction(type, variable)
        appender += InvokeInstanceMethodInstruction(type, "readInt", "()I", Opcodes.INVOKEINTERFACE)
    }

    override fun readLong(appender: BytecodeAppender) {
        appender += LoadVariableInstruction(type, variable)
        appender += InvokeInstanceMethodInstruction(type, "readLong", "()J", Opcodes.INVOKEINTERFACE)
    }

    override fun readByte(appender: BytecodeAppender) {
        appender += LoadVariableInstruction(type, variable)
        appender += InvokeInstanceMethodInstruction(type, "readByte", "()B", Opcodes.INVOKEINTERFACE)
    }

    override fun readShort(appender: BytecodeAppender) {
        appender += LoadVariableInstruction(type, variable)
        appender += InvokeInstanceMethodInstruction(type, "readShort", "()S", Opcodes.INVOKEINTERFACE)
    }

    override fun readBoolean(appender: BytecodeAppender) {
        appender += LoadVariableInstruction(type, variable)
        appender += InvokeInstanceMethodInstruction(type, "readBoolean", "()Z", Opcodes.INVOKEINTERFACE)
    }

    override fun readChar(appender: BytecodeAppender) {
        appender += LoadVariableInstruction(type, variable)
        appender += InvokeInstanceMethodInstruction(type, "readChar", "()C", Opcodes.INVOKEINTERFACE)
    }

    override fun readString(appender: BytecodeAppender) {
        appender += LoadVariableInstruction(type, variable)
        appender += InvokeInstanceMethodInstruction(type, "readUTF", "()Ljava/lang/String;", Opcodes.INVOKEINTERFACE)
    }

    override fun readFloat(appender: BytecodeAppender) {
        appender += LoadVariableInstruction(type, variable)
        appender += InvokeInstanceMethodInstruction(type, "readFloat", "()F", Opcodes.INVOKEINTERFACE)
    }

    override fun readDouble(appender: BytecodeAppender) {
        appender += LoadVariableInstruction(type, variable)
        appender += InvokeInstanceMethodInstruction(type, "readDouble", "()D", Opcodes.INVOKEINTERFACE)
    }
}