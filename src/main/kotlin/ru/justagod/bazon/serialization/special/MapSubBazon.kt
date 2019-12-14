package ru.justagod.bazon.serialization.special

import ru.justagod.bazon.data.FlowReader
import ru.justagod.bazon.serialization.DeserializationContext
import ru.justagod.bazon.serialization.SerializationContext
import ru.justagod.bazon.serialization.SubBazon
import ru.justagod.bazon.stackManipulation.*
import ru.justagod.model.*
import org.objectweb.asm.Opcodes
import java.lang.IllegalArgumentException

object MapSubBazon : SubBazon {

    private val hashMapType = ClassTypeReference(HashMap::class.java.name)
    private val abstractMapType = ClassTypeReference(AbstractMap::class.java.name)
    private val mapType = ClassTypeReference(Map::class.java.name)

    override fun serialize(model: TypeReference, appender: BytecodeAppender, context: SerializationContext, parent: AbstractModel) {
        assert(parent is FieldModel && parent.type is ParameterizedTypeModel)
        parent as FieldModel
        parent.type as ParameterizedTypeModel
        val keyType = parent.type.parameters[0].getMyType()
        val valueType = parent.type.parameters[1].getMyType()
        appender += CastInstruction(model, mapType)
        context.workWithTop(mapType, appender) {
            val iterator = NewVariableInstruction(ClassTypeReference(Iterator::class.java.name))
            update(appender)
            appender += InvokeInstanceMethodInstruction(mapType, "entrySet", "()Ljava/util/Set;", Opcodes.INVOKEINTERFACE)
            appender += CastInstruction(ClassTypeReference("java.util.Set"), ClassTypeReference(Iterable::class.java.name))
            appender += InvokeInstanceMethodInstruction(
                    ClassTypeReference(Iterable::class.java.name),
                    "iterator",
                    "()Ljava/util/Iterator;",
                    Opcodes.INVOKEINTERFACE
            )
            appender += iterator
            appender += PutVariableInstruction(ClassTypeReference(Iterator::class.java.name), iterator)
            val builder = appender.makeSetBuilder()
            update(builder)
            builder += InvokeInstanceMethodInstruction(mapType, "size", "()I", Opcodes.INVOKEINTERFACE, true)
            context.workWithArray(null, builder.build(), appender) { arrWorker ->
                BytecodeUtils.makeFor(appender, builder.build()) {
                    appender += LoadVariableInstruction(ClassTypeReference(Iterator::class.java.name), iterator)
                    appender += InvokeInstanceMethodInstruction(
                            ClassTypeReference(Iterator::class.java.name),
                            "next",
                            "()Ljava/lang/Object;",
                            Opcodes.INVOKEINTERFACE
                    )
                    appender += CastInstruction(OBJECT_REFERENCE, ClassTypeReference("java.util.Map\$Entry"))

                    context.workWithTop(ClassTypeReference("java.util.Map\$Entry"), appender) {
                        val keyBuilder = appender.makeSetBuilder()
                        update(keyBuilder)
                        keyBuilder += InvokeInstanceMethodInstruction(
                                ClassTypeReference("java.util.Map\$Entry"),
                                "getKey",
                                "()Ljava/lang/Object;",
                                Opcodes.INVOKEINTERFACE
                        )
                        keyBuilder += CastInstruction(OBJECT_REFERENCE, keyType)

                        val valueBuilder = appender.makeSetBuilder()
                        update(valueBuilder)
                        valueBuilder += InvokeInstanceMethodInstruction(
                                ClassTypeReference("java.util.Map\$Entry"),
                                "getValue",
                                "()Ljava/lang/Object;",
                                Opcodes.INVOKEINTERFACE
                        )
                        valueBuilder += CastInstruction(OBJECT_REFERENCE, valueType)
                        arrWorker.workWithObject {
                            it.write(keyType, keyBuilder.build(), parent, "key", appender)
                            it.write(valueType, valueBuilder.build(), parent, "value", appender)
                        }
                    }
                }
            }
        }
    }

    override fun deserialize(model: TypeReference, appender: BytecodeAppender, context: DeserializationContext, parent: AbstractModel) {
        assert(parent is FieldModel && parent.type is ParameterizedTypeModel)
        if (model != hashMapType && model != mapType && model != abstractMapType) {
            throw IllegalArgumentException("Unsupported map field type $model")
        }

        parent as FieldModel
        parent.type as ParameterizedTypeModel
        val keyType = parent.type.parameters[0].getMyType()
        val valueType = parent.type.parameters[1].getMyType()

        val instanceBuilder = InstructionSet.Builder(BytecodeStack(PrimitiveTypeReference(PrimitiveKind.INT)))
        val variable = NewVariableInstruction(model)
        appender += variable
        context.workWithTop(PrimitiveTypeReference(PrimitiveKind.INT), instanceBuilder) {
            instanceBuilder += NewInstanceInstruction(hashMapType)
            instanceBuilder += DupInstruction(hashMapType)
            update(instanceBuilder)
            instanceBuilder += InvokeInstanceMethodInstruction(hashMapType, "<init>", "(I)V", Opcodes.INVOKESPECIAL)
            instanceBuilder += PutVariableInstruction(hashMapType, variable)
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
            appender += LoadVariableInstruction(hashMapType, variable)
            appender += CastInstruction(hashMapType, mapType)
            it.readNext(keyType, appender)
            appender += CastInstruction(keyType, OBJECT_REFERENCE)
            it.readNext(valueType, appender)
            appender += CastInstruction(valueType, OBJECT_REFERENCE)
            appender += InvokeInstanceMethodInstruction(
                    mapType,
                    "put",
                    "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                    Opcodes.INVOKEINTERFACE,
                    true
            )
            appender += PopInstruction(OBJECT_REFERENCE)
        }
        appender += LoadVariableInstruction(mapType, variable)
    }
}