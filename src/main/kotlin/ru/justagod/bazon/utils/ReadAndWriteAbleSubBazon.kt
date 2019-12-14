package ru.justagod.bazon.utils

import org.objectweb.asm.Opcodes
import ru.justagod.bazon.serialization.DeserializationContext
import ru.justagod.bazon.serialization.SerializationContext
import ru.justagod.bazon.serialization.SubBazon
import ru.justagod.bazon.stackManipulation.BytecodeAppender
import ru.justagod.bazon.stackManipulation.BytecodeUtils
import ru.justagod.bazon.stackManipulation.InvokeInstanceMethodInstruction
import ru.justagod.bazon.stackManipulation.LoadVariableInstruction
import ru.justagod.model.AbstractModel
import ru.justagod.model.ClassTypeReference
import ru.justagod.model.TypeReference

object ReadAndWriteAbleSubBazon : SubBazon {
    override fun serialize(model: TypeReference, appender: BytecodeAppender, context: SerializationContext, parent: AbstractModel) {
        appender += LoadVariableInstruction(ClassTypeReference("java.io.DataOutput"), 1)
        appender += InvokeInstanceMethodInstruction(model, "write", "(Ljava/io/DataOutput;)V", Opcodes.INVOKEVIRTUAL)
    }

    override fun deserialize(model: TypeReference, appender: BytecodeAppender, context: DeserializationContext, parent: AbstractModel) {
        BytecodeUtils.makeDefaultInstance(model as ClassTypeReference, appender)
        context.workWithTop(model, appender) {
            update(appender)
            appender += LoadVariableInstruction(ClassTypeReference("java.io.DataInput"), 1)
            appender += InvokeInstanceMethodInstruction(model, "read", "(Ljava/io/DataInput;)V", Opcodes.INVOKEVIRTUAL)
            update(appender)
        }
    }
}