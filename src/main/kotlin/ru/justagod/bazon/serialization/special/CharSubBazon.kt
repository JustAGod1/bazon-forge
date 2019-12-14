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

object CharSubBazon : SubBazon {

    private val type = ClassTypeReference(Character::class.java.name)

    override fun deserialize(model: TypeReference, appender: BytecodeAppender, context: DeserializationContext, parent: AbstractModel) {
        appender += NewInstanceInstruction(ru.justagod.bazon.serialization.special.CharSubBazon.type)
        appender += DupInstruction(ru.justagod.bazon.serialization.special.CharSubBazon.type)
        context.reader.readChar(appender)
        appender += InvokeInstanceMethodInstruction(ru.justagod.bazon.serialization.special.CharSubBazon.type, "<init>", "(C)V", Opcodes.INVOKESPECIAL)
    }

    override fun serialize(model: TypeReference, appender: BytecodeAppender, context: SerializationContext, parent: AbstractModel) {
        val valueBuilder = appender.makeSetBuilder()
        context.workWithTop(ru.justagod.bazon.serialization.special.CharSubBazon.type, appender) {
            update(valueBuilder)
            valueBuilder += InvokeInstanceMethodInstruction(ru.justagod.bazon.serialization.special.CharSubBazon.type, "charValue", "()C", Opcodes.INVOKEVIRTUAL)
        }
        context.writer.writeChar(appender, valueBuilder.build())

    }
}