package ru.justagod.bazon.serialization

import ru.justagod.bazon.Bazon
import ru.justagod.bazon.stackManipulation.*
import ru.justagod.model.*
import ru.justagod.model.factory.ModelFactory
import ru.justagod.utils.AsmUtil
import ru.justagod.utils.PrimitivesAdapter

open class ContextBase(
        protected val parent: Bazon,
        protected val factory: ModelFactory,
        protected val myAppender: BytecodeAppender,
        protected val useReflection: Boolean
) {

    fun workWithTop(type: TypeReference, appender: BytecodeAppender, worker: ru.justagod.bazon.serialization.ContextBase.TopWorker.() -> Unit) {
        val variable = NewVariableInstruction(type)
        appender += variable
        appender += PutVariableInstruction(type, variable)
        val updater = ru.justagod.bazon.serialization.ContextBase.TopWorker(type, variable, useReflection)
        updater.worker()
    }

    fun fetchModel(type: ClassTypeReference, field: AbstractModel) = factory.makeModel(type, field)


    class TopWorker(
            private val type: TypeReference,
            private val variable: NewVariableInstruction,
            private val useReflection: Boolean
    ) {
        fun update(appender: BytecodeAppender) {
            appender += LoadVariableInstruction(type, variable)
        }

        fun fetchField(field: FieldModel, appender: BytecodeAppender) {
            assert(type is ClassTypeReference)
            if (useReflection && (field.access.final || !field.access.public)) {
                appender += PopInstruction(type)
                val builder = InstructionSet.Builder(BytecodeStack())
                update(builder)
                builder += CastInstruction(type, OBJECT_REFERENCE)
                AsmUtil.getFieldValueViaReflect(
                        type as ClassTypeReference,
                        field.name,
                        appender,
                        builder.build()
                )
                val fieldType = field.type.getMyType()
                if (fieldType is PrimitiveTypeReference) {
                    appender += CastInstruction(
                            OBJECT_REFERENCE,
                            PrimitivesAdapter.getWrapperForPrimitive(fieldType.kind)
                    )
                    PrimitivesAdapter.unwrap(appender, fieldType.kind)
                } else {
                    appender += CastInstruction(OBJECT_REFERENCE, fieldType)
                }

            } else {
                appender += FieldGetInstruction(type as ClassTypeReference, field.name, field.type.getMyType())
            }
        }


    }

    inline fun tryWithRethrow(msg: String? = null, body: () -> Unit) {
        try {
            body()
        } catch (e: Throwable) {
            throw RuntimeException(msg, e)
        }
    }
}