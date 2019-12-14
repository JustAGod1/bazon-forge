package ru.justagod.bazon.serialization

import ru.justagod.bazon.Bazon
import ru.justagod.bazon.data.FlowWriter
import ru.justagod.bazon.data.TagWriter
import ru.justagod.bazon.data.Writer
import ru.justagod.bazon.stackManipulation.*
import ru.justagod.model.*
import ru.justagod.model.factory.ModelFactory
import org.objectweb.asm.Opcodes
import java.lang.RuntimeException

class SerializationContext(
        parent: Bazon,
        appender: BytecodeAppender,
        factory: ModelFactory,
        val writer: ru.justagod.bazon.data.Writer,
        useReflection: Boolean
) : ContextBase(parent, factory, appender, useReflection) {

    fun autoSerialize(model: ClassModel, appender: BytecodeAppender = myAppender) {
        workWithTop(model.name, appender) {
            workWithObject(model.name.simpleName) {
                for (field in model.fields) {
                    tryWithRethrow("Exception while serializing field \"${model.name.name}.${field.name}\"") {
                        if (!(field.access.static || field.access.transient)) {
                            val fieldWriter: ru.justagod.bazon.serialization.ContextBase.TopWorker.() -> Unit = {
                                val builder = appender.makeSetBuilder()
                                update(builder)
                                fetchField(field, builder)
                                writeValue(field.type.getMyType(), field.name, field, builder.build(), appender)
                            }
                            if (field.nullable) insideNullity(appender, field, fieldWriter)
                            else fieldWriter()
                        }
                    }
                }
            }
        }
    }

    private fun ru.justagod.bazon.serialization.ContextBase.TopWorker.insideNullity(appender: BytecodeAppender, fieldModel: FieldModel, block: ru.justagod.bazon.serialization.ContextBase.TopWorker.() -> Unit) {
        if (fieldModel.nullable) {
            update(appender)
            val elseLabel = LabelInstruction()
            val endLabel = LabelInstruction()
            fetchField(fieldModel, appender)
            appender += SingleIfInstruction(fieldModel.type.getMyType(), Opcodes.IFNULL, elseLabel)
            writeValue(
                    PrimitiveTypeReference(PrimitiveKind.BOOLEAN),
                    "${fieldModel.name}\$isNull",
                    fieldModel,
                    ru.justagod.bazon.stackManipulation.InstructionSet.create(IntInsnInstruction(Opcodes.ICONST_0), IntToBooleanInstruction),
                    appender
            )
            block()
            appender += GoToInstruction(endLabel)
            appender += elseLabel
            writeValue(
                    PrimitiveTypeReference(PrimitiveKind.BOOLEAN),
                    "${fieldModel.name}\$isNull",
                    fieldModel,
                    ru.justagod.bazon.stackManipulation.InstructionSet.create(IntInsnInstruction(Opcodes.ICONST_1), IntToBooleanInstruction),
                    appender
            )
            appender += endLabel
        }
    }

    private fun writeValue(type: TypeReference, name: String?, parent: AbstractModel?, value: ru.justagod.bazon.stackManipulation.InstructionSet, appender: BytecodeAppender) {
        if (name != null && writer is ru.justagod.bazon.data.TagWriter) {
            writer.name(name)
        }
        when (type) {
            is PrimitiveTypeReference -> {
                when (type.kind) {
                    PrimitiveKind.BYTE -> writer.writeByte(appender, value)
                    PrimitiveKind.SHORT -> writer.writeShort(appender, value)
                    PrimitiveKind.INT -> writer.writeInt(appender, value)
                    PrimitiveKind.LONG -> writer.writeLong(appender, value)
                    PrimitiveKind.FLOAT -> writer.writeFloat(appender, value)
                    PrimitiveKind.DOUBLE -> writer.writeDouble(appender, value)
                    PrimitiveKind.CHAR -> writer.writeChar(appender, value)
                    PrimitiveKind.BOOLEAN -> writer.writeBoolean(appender, value)
                }
            }
            is ClassTypeReference -> {
                appender += value
                val specialSerializer = this.parent.subBazons.findEntry(type)
                if (specialSerializer != null) {
                    specialSerializer.serialize(type, appender, this, parent!!)
                } else {
                    val model = fetchModel(type, parent!!)
                    if (model.enum) {
                        serializeEnum(type, value, appender)
                    } else {
                        throw RuntimeException("Can not serialize unknown type $type, " +
                                "you can register serializer in Bazon class")
                    }
                }
            }
            is ArrayTypeReference -> {
                appender += value
                val specialSerializer = this.parent.subBazons.findEntry(type)
                if (specialSerializer != null) {
                    specialSerializer.serialize(type, appender, this, parent!!)
                } else {
                    serializeArray(name!!, type, parent!!, appender)
                }
            }
        }
    }

    private fun serializeEnum(type: ClassTypeReference, value: ru.justagod.bazon.stackManipulation.InstructionSet, appender: BytecodeAppender) {
        val builder = appender.makeSetBuilder()
        builder += value
        builder += InvokeInstanceMethodInstruction(type, "ordinal", "()I", Opcodes.INVOKEVIRTUAL)
        writeValue(PrimitiveTypeReference(PrimitiveKind.INT), null, null, builder.build(), appender)
    }

    fun <R> workWithArray(name: String?, size: ru.justagod.bazon.stackManipulation.InstructionSet, appender: BytecodeAppender, block: (ru.justagod.bazon.serialization.SerializationContext.ArrayWorker) -> R): R {
        if (writer is ru.justagod.bazon.data.FlowWriter) {
            writeValue(PrimitiveTypeReference(PrimitiveKind.INT), null, null, size, appender)
        }
        if (writer is ru.justagod.bazon.data.TagWriter) {
            if (name != null) writer.name(name)
            writer.startArray()
        }
        val worker = ArrayWorker()
        val result = block(worker)
        if (writer is ru.justagod.bazon.data.TagWriter) {
            writer.endArray()
        }
        return result
    }

    fun <R> workWithObject(name: String, block: (ru.justagod.bazon.serialization.SerializationContext.ObjectWorker) -> R): R {
        if (writer is ru.justagod.bazon.data.TagWriter) {
            writer.name(name)
            writer.startObject()
        }
        val worker = ObjectWorker()
        val result = block(worker)
        if (writer is ru.justagod.bazon.data.TagWriter) {
            writer.endObject()
        }
        return result
    }

    private fun serializeArray(name: String, type: ArrayTypeReference, parent: AbstractModel, appender: BytecodeAppender) {
        val variable = NewVariableInstruction(type)
        appender += variable
        appender += PutVariableInstruction(type, variable)
        var builder = appender.makeSetBuilder()
        builder += LoadVariableInstruction(type, variable)
        builder += ArrayLengthInstruction(type)
        workWithArray(name, builder.build(), appender) {
            appender += LoadVariableInstruction(type, variable)
            BytecodeUtils.makeArrayFor(type, appender) {
                builder = appender.makeSetBuilder()
                putValue(builder)
                writeValue(type.arrayType, null, parent, builder.build(), appender)
            }
        }
    }


    inner class ObjectWorker {

        fun write(type: TypeReference, value: ru.justagod.bazon.stackManipulation.InstructionSet, parent: AbstractModel, name: String, appender: BytecodeAppender) {
            writeValue(type, name, parent, value, appender)
        }
    }

    inner class ArrayWorker {

        fun write(value: ru.justagod.bazon.stackManipulation.InstructionSet, type: TypeReference, parent: AbstractModel?, appender: BytecodeAppender) {
            writeValue(type, null, parent, value, appender)
        }

        fun <R> workWithObject(block: (ru.justagod.bazon.serialization.SerializationContext.ObjectWorker) -> R): R {
            if (writer is ru.justagod.bazon.data.TagWriter) {
                writer.startObject()
            }
            val worker = ObjectWorker()
            val result = block(worker)
            if (writer is ru.justagod.bazon.data.TagWriter) {
                writer.endObject()
            }
            return result
        }
    }
}