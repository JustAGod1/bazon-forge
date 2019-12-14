package ru.justagod.bazon.serialization

import ru.justagod.bazon.Bazon
import ru.justagod.bazon.data.Reader
import ru.justagod.bazon.data.TagReader
import ru.justagod.model.factory.ModelFactory
import ru.justagod.bazon.stackManipulation.*
import ru.justagod.model.*
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.LocalVariablesSorter
import ru.justagod.utils.AsmUtil
import ru.justagod.utils.PrimitivesAdapter
import java.lang.RuntimeException

class DeserializationContext(
        parent: Bazon,
        appender: BytecodeAppender,
        factory: ModelFactory,
        val reader: Reader,
        useReflection: Boolean
) : ContextBase(parent, factory, appender, useReflection) {

    @JvmOverloads
    fun autoDeserialize(model: ClassModel, appender: BytecodeAppender = myAppender) {
        workWithTop(model.name, appender) {
            workWithObject(null) {
                for (field in model.fields) {
                    tryWithRethrow("Exception while deserializing field \"${model.name.name}.${field.name}\"") {
                        if (!(field.access.static || field.access.transient)) {
                            val fieldReader: TopWorker.() -> Unit = {
                                if (useReflection && (field.access.final || !field.access.public)) {
                                    val builder = InstructionSet.Builder(BytecodeStack())
                                    update(builder)
                                    builder += CastInstruction(model.name, OBJECT_REFERENCE)
                                    it.readNext(field.name, field.type.getMyType(), field, builder)
                                    if (field.type.getMyType() is PrimitiveTypeReference) {
                                        val wrapper = PrimitivesAdapter.wrap(
                                                builder,
                                                (field.type.getMyType() as PrimitiveTypeReference).kind
                                        )
                                        builder += CastInstruction(wrapper, OBJECT_REFERENCE)
                                    } else {
                                        builder += CastInstruction(field.type.getMyType(), OBJECT_REFERENCE)
                                    }


                                    AsmUtil.setFieldValueViaReflect(model.name, field.name, appender, builder.build())
                                } else {
                                    update(appender)
                                    it.readNext(field.name, field.type.getMyType(), field, appender)

                                    appender += FieldPutInstruction(model.name, field.name, field.type.getMyType())
                                }
                            }
                            if (field.nullable) insideNullity(appender, field, fieldReader)
                            else fieldReader()
                        }
                    }
                }
            }
        }
    }

    private fun TopWorker.insideNullity(appender: BytecodeAppender, fieldModel: FieldModel, block: TopWorker.() -> Unit) {
        readValue(PrimitiveTypeReference(PrimitiveKind.BOOLEAN), "${fieldModel.name}\$isNull", fieldModel, appender)
        val elseLabel = LabelInstruction()
        appender += SingleIfInstruction(PrimitiveTypeReference(PrimitiveKind.BOOLEAN), Opcodes.IFNE, elseLabel)
        block()
        appender += elseLabel
    }

    private fun readValue(type: TypeReference, name: String?, parent: AbstractModel?, appender: BytecodeAppender) {
        if (name != null && reader is TagReader) {
            reader.name(name)
        }
        when (type) {
            is PrimitiveTypeReference -> {
                when (type.kind) {
                    PrimitiveKind.BYTE -> reader.readByte(appender)
                    PrimitiveKind.SHORT -> reader.readShort(appender)
                    PrimitiveKind.INT -> reader.readInt(appender)
                    PrimitiveKind.LONG -> reader.readLong(appender)
                    PrimitiveKind.FLOAT -> reader.readFloat(appender)
                    PrimitiveKind.DOUBLE -> reader.readDouble(appender)
                    PrimitiveKind.CHAR -> reader.readChar(appender)
                    PrimitiveKind.BOOLEAN -> reader.readBoolean(appender)
                }
            }
            is ClassTypeReference -> {
                val specialSerializer = this.parent.subBazons.findEntry(type)
                if (specialSerializer != null) {
                    specialSerializer.deserialize(type, appender, this, parent!!)
                } else {
                    val model = fetchModel(type, parent!!)
                    if (model.enum) {
                        deserializeEnum(type, appender)
                    } else {
                        throw RuntimeException("Can not deserialize unknown type $type, " +
                                "you can register deserializer in Bazon class")
                    }
                }
            }
            is ArrayTypeReference -> {
                val specialSerializer = this.parent.subBazons.findEntry(type)
                if (specialSerializer != null) {
                    specialSerializer.deserialize(type, appender, this, parent!!)
                } else {
                    deserializeArray(name!!, type, parent!!, appender)
                }
            }
        }
    }

    private fun deserializeEnum(type: ClassTypeReference, appender: BytecodeAppender) {
        appender += InvokeStaticMethodInstruction(type, "values", "()[" + type.toASMType().descriptor)
        readValue(PrimitiveTypeReference(PrimitiveKind.INT), null, null, appender)
        appender += ArrayVariableLoadInstruction(ArrayTypeReference(type))
    }

    private fun deserializeArray(name: String, type: ArrayTypeReference, parent: AbstractModel, appender: BytecodeAppender) {
        val builder = InstructionSet.Builder(BytecodeStack(PrimitiveTypeReference(PrimitiveKind.INT)))
        val variable = NewVariableInstruction(type)
        builder += variable
        createInstance(type, builder)
        builder += PutVariableInstruction(type, variable)
        val sizeVariable = NewVariableInstruction(PrimitiveTypeReference(PrimitiveKind.INT))
        appender += sizeVariable
        readValue(PrimitiveTypeReference(PrimitiveKind.INT), null, null, appender)
        appender += PutVariableInstruction(PrimitiveTypeReference(PrimitiveKind.INT), sizeVariable)

        workWithArray(
                name,
                parent,
                builder.build(),
                InstructionSet.create(LoadVariableInstruction(PrimitiveTypeReference(PrimitiveKind.INT), sizeVariable)),
                appender
        ) {
            appender += LoadVariableInstruction(type, variable)
            it.putIndex(appender)
            it.readNext(type.arrayType, appender)

            appender += ArrayVariableStoreInstruction(type)
        }
        appender += LoadVariableInstruction(type, variable)
    }

    fun workWithArray(
            name: String?,
            parent: AbstractModel,
            instanceMaker: InstructionSet,
            size: InstructionSet,
            appender: BytecodeAppender,
            block: (ArrayWorker) -> Unit
    ) {
        val sizeFetcher = if (reader is TagReader) {
            name?.let { reader.name(it) }
            reader.startArray()
            reader.arraySize()
        } else size
        appender += sizeFetcher
        appender += instanceMaker
        BytecodeUtils.makeFor(appender, size) {
            val worker = ArrayWorker(it, parent)
            block(worker)
        }
        if (reader is TagReader) reader.endArray()
    }

    fun workWithObject(name: String?, block: (ObjectWorker) -> Unit) {
        if (reader is TagReader) {
            name?.let { reader.name(it) }
            reader.startObject()
        }
        val worker = ObjectWorker()
        block(worker)
        if (reader is TagReader) reader.endObject()
    }

    private val modelsBuffer = hashMapOf<ClassTypeReference, ClassModel>()
    fun createInstance(type: ClassTypeReference, appender: BytecodeAppender) {
        fun getModel(type: ClassTypeReference): ClassModel {
            if (type in modelsBuffer) return modelsBuffer[type]!!
            val model = factory.makeModel(type, null)
            modelsBuffer[type] = model
            return model
        }

        val factory = parent.factories.findEntry(type)
        if (factory != null) {
            factory.makeInstance(type, appender)
            return
        }
        val model = getModel(type)
        if (!model.hasDefaultConstructor) error("${type.name} hasn't a default constructor")
        BytecodeUtils.makeDefaultInstance(type, appender)
    }

    fun createInstance(type: ArrayTypeReference, appender: BytecodeAppender) {
        BytecodeUtils.makeArray(type.arrayType, appender)
    }

    inner class ArrayWorker(private val index: NewVariableInstruction, private val parent: AbstractModel) {

        fun readNext(type: TypeReference, appender: BytecodeAppender) {
            readValue(type, null, parent, appender)
        }

        fun putIndex(appender: BytecodeAppender) {
            appender += LoadVariableInstruction(PrimitiveTypeReference(PrimitiveKind.INT), index)
        }
    }

    inner class ObjectWorker {

        fun readNext(name: String, type: TypeReference, parent: AbstractModel, appender: BytecodeAppender) {
            readValue(type, name, parent, appender)
        }

    }
}