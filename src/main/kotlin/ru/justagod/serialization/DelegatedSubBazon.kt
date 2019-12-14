package ru.justagod.serialization

import org.objectweb.asm.Type
import org.objectweb.asm.Type.getType
import ru.justagod.bazon.serialization.DeserializationContext
import ru.justagod.bazon.serialization.SerializationContext
import ru.justagod.bazon.serialization.SubBazon
import ru.justagod.bazon.stackManipulation.BytecodeAppender
import ru.justagod.bazon.stackManipulation.InvokeStaticMethodInstruction
import ru.justagod.bazon.stackManipulation.LoadVariableInstruction
import ru.justagod.bazon.stackManipulation.toReference
import ru.justagod.model.AbstractModel
import ru.justagod.model.ClassTypeReference
import ru.justagod.model.TypeReference

class DelegatedSubBazon(
        val type: TypeReference,
        private val klass: ClassTypeReference,
        private val readName: String,
        private val writeName: String
) : SubBazon {

    override fun serialize(model: TypeReference, appender: BytecodeAppender, context: SerializationContext, parent: AbstractModel) {
        appender += LoadVariableInstruction(DATA_OUTPUT.toReference(), 1)
        appender += InvokeStaticMethodInstruction(klass, writeName,
                "(" + type.toASMType().descriptor + DATA_OUTPUT.descriptor + ")V", true)
    }

    override fun deserialize(model: TypeReference, appender: BytecodeAppender, context: DeserializationContext, parent: AbstractModel) {
        appender += LoadVariableInstruction(DATA_INPUT.toReference(), 1)
        appender += InvokeStaticMethodInstruction(klass, readName,
                "(" + DATA_INPUT.descriptor + ")" + type.toASMType().descriptor, true)
    }

    companion object {
        val DATA_INPUT = getType("Ljava/io/DataInput;")!!
        val DATA_OUTPUT = getType("Ljava/io/DataOutput;")!!
    }
}