package ru.justagod.bazon.serialization.special

import ru.justagod.model.AbstractModel
import ru.justagod.model.ClassTypeReference
import ru.justagod.model.TypeReference
import ru.justagod.bazon.serialization.DeserializationContext
import ru.justagod.bazon.serialization.SerializationContext
import ru.justagod.bazon.serialization.SubBazon
import ru.justagod.bazon.stackManipulation.BytecodeAppender
import ru.justagod.bazon.stackManipulation.DupInstruction
import ru.justagod.bazon.stackManipulation.InvokeInstanceMethodInstruction
import ru.justagod.bazon.stackManipulation.NewInstanceInstruction
import org.objectweb.asm.Opcodes
import java.lang.Boolean

object BooleanSubBazon : SubBazon {

    private val type = ClassTypeReference(Boolean::class.java.name)

    override fun deserialize(model: TypeReference, appender: BytecodeAppender, context: DeserializationContext, parent: AbstractModel) {
        appender += NewInstanceInstruction(ru.justagod.bazon.serialization.special.BooleanSubBazon.type)
        appender += DupInstruction(ru.justagod.bazon.serialization.special.BooleanSubBazon.type)
        context.reader.readBoolean(appender)
        appender += InvokeInstanceMethodInstruction(ru.justagod.bazon.serialization.special.BooleanSubBazon.type, "<init>", "(Z)V", Opcodes.INVOKESPECIAL)
    }

    override fun serialize(model: TypeReference, appender: BytecodeAppender, context: SerializationContext, parent: AbstractModel) {
        val valueBuilder = appender.makeSetBuilder()
        context.workWithTop(ru.justagod.bazon.serialization.special.BooleanSubBazon.type, appender) {
            update(valueBuilder)
            valueBuilder += InvokeInstanceMethodInstruction(ru.justagod.bazon.serialization.special.BooleanSubBazon.type, "byteValue", "()Z", Opcodes.INVOKEVIRTUAL)
        }
        context.writer.writeBoolean(appender, valueBuilder.build())

    }
}