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

object IntegerSubBazon : SubBazon {

    private val type = ClassTypeReference(Integer::class.java.name)

    override fun deserialize(model: TypeReference, appender: BytecodeAppender, context: DeserializationContext, parent: AbstractModel) {
        appender += NewInstanceInstruction(ru.justagod.bazon.serialization.special.IntegerSubBazon.type)
        appender += DupInstruction(ru.justagod.bazon.serialization.special.IntegerSubBazon.type)
        context.reader.readInt(appender)
        appender += InvokeInstanceMethodInstruction(ru.justagod.bazon.serialization.special.IntegerSubBazon.type, "<init>", "(I)V", Opcodes.INVOKESPECIAL)
    }

    override fun serialize(model: TypeReference, appender: BytecodeAppender, context: SerializationContext, parent: AbstractModel) {
        val valueBuilder = appender.makeSetBuilder()
        context.workWithTop(ru.justagod.bazon.serialization.special.IntegerSubBazon.type, appender) {
            update(valueBuilder)
            valueBuilder += InvokeInstanceMethodInstruction(ru.justagod.bazon.serialization.special.IntegerSubBazon.type, "intValue", "()I", Opcodes.INVOKEVIRTUAL)
        }
        context.writer.writeInt(appender, valueBuilder.build())

    }
}