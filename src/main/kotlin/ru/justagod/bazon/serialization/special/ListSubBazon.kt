package ru.justagod.bazon.serialization.special

import ru.justagod.bazon.data.FlowReader
import ru.justagod.bazon.serialization.DeserializationContext
import ru.justagod.bazon.serialization.SerializationContext
import ru.justagod.bazon.serialization.SubBazon
import ru.justagod.bazon.stackManipulation.*
import ru.justagod.model.*
import org.objectweb.asm.Opcodes
import java.lang.IllegalArgumentException

object ListSubBazon : SubBazon {

    private val arrayListType = ClassTypeReference(ArrayList::class.java.name)
    private val abstractListType = ClassTypeReference(AbstractList::class.java.name)
    private val listType = ClassTypeReference(List::class.java.name)

    init {
    }

    override fun deserialize(model: TypeReference, appender: BytecodeAppender, context: DeserializationContext, parent: AbstractModel) {
        assert(parent is FieldModel && parent.type is ParameterizedTypeModel)
        if (model != arrayListType && model != abstractListType && model != listType) {
            throw IllegalArgumentException("Unsupported list field type $model")
        }

        parent as FieldModel
        val type = (parent.type as ParameterizedTypeModel).parameters[0].getMyType()
        val instanceBuilder = InstructionSet.Builder(BytecodeStack(PrimitiveTypeReference(PrimitiveKind.INT)))
        val variable = NewVariableInstruction(model)
        appender += variable
        context.workWithTop(PrimitiveTypeReference(PrimitiveKind.INT), instanceBuilder) {
            instanceBuilder += NewInstanceInstruction(arrayListType)
            instanceBuilder += DupInstruction(arrayListType)
            update(instanceBuilder)
            instanceBuilder += InvokeInstanceMethodInstruction(arrayListType, "<init>", "(I)V", Opcodes.INVOKESPECIAL)
            instanceBuilder += PutVariableInstruction(arrayListType, variable)
        }
        val sizeBuilder = appender.makeSetBuilder()
        if (context.reader is FlowReader) {
            context.reader.readInt(appender)
            val sizeVariable = NewVariableInstruction(PrimitiveTypeReference(PrimitiveKind.INT))
            appender += sizeVariable
            appender += PutVariableInstruction(PrimitiveTypeReference(PrimitiveKind.INT), sizeVariable)
            sizeBuilder += LoadVariableInstruction(PrimitiveTypeReference(PrimitiveKind.INT), sizeVariable)
        }
        context.workWithArray(null, parent, instanceBuilder.build(), sizeBuilder.build(), appender) {
            appender += LoadVariableInstruction(arrayListType, variable)
            appender += CastInstruction(arrayListType, ClassTypeReference(List::class.java.name))
            it.readNext(type, appender)
            appender += CastInstruction(type, OBJECT_REFERENCE)
            appender += InvokeInstanceMethodInstruction(
                    ClassTypeReference(List::class.java.name),
                    "add",
                    "(Ljava/lang/Object;)Z",
                    Opcodes.INVOKEINTERFACE,
                    true
            )
            appender += PopInstruction(PrimitiveTypeReference(PrimitiveKind.BOOLEAN))
        }
        appender += LoadVariableInstruction(arrayListType, variable)
        appender += CastInstruction(arrayListType, model)
    }

    override fun serialize(model: TypeReference, appender: BytecodeAppender, context: SerializationContext, parent: AbstractModel) {
        assert(parent is FieldModel && parent.type is ParameterizedTypeModel)
        parent as FieldModel
        val type = (parent.type as ParameterizedTypeModel).parameters[0]
        appender += CastInstruction(model, listType)
        context.workWithTop(listType, appender) {
            val builder = appender.makeSetBuilder()
            update(builder)
            builder += InvokeInstanceMethodInstruction(listType, "size", "()I", Opcodes.INVOKEINTERFACE, true)
            context.workWithArray(null, builder.build(), appender) { arrWorker ->
                BytecodeUtils.makeFor(appender, builder.build()) {
                    val valueBuilder = appender.makeSetBuilder()
                    update(valueBuilder)
                    valueBuilder += LoadVariableInstruction(PrimitiveTypeReference(PrimitiveKind.INT), it)
                    valueBuilder += InvokeInstanceMethodInstruction(listType, "get", "(I)Ljava/lang/Object;", Opcodes.INVOKEINTERFACE)
                    valueBuilder += CastInstruction(OBJECT_REFERENCE, type.getMyType())
                    arrWorker.write(valueBuilder.build(), type.getMyType(), parent, appender)
                }
            }
        }
    }
}