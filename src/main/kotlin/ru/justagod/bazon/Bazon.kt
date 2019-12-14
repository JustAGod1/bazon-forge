package ru.justagod.bazon

import org.objectweb.asm.commons.LocalVariablesSorter
import ru.justagod.bazon.data.Reader
import ru.justagod.bazon.data.Writer
import ru.justagod.bazon.serialization.*
import ru.justagod.bazon.serialization.special.*
import ru.justagod.bazon.stackManipulation.BytecodeAppender
import ru.justagod.bazon.stackManipulation.BytecodeStack
import ru.justagod.bazon.stackManipulation.SimpleAppender
import ru.justagod.bazon.utils.ClassHandlerRegistry
import ru.justagod.model.ClassTypeReference
import ru.justagod.model.InheritanceHelper
import ru.justagod.model.factory.ModelFactory
import java.util.AbstractMap
import java.util.ArrayList

open class Bazon(private val modelFactory: ModelFactory) {

    val inheritance = InheritanceHelper(modelFactory)
    val subBazons = ClassHandlerRegistry<SubBazon>(inheritance)
    val factories = ClassHandlerRegistry<InstanceFactory>(inheritance)

    init {
        subBazons.register(ClassTypeReference(List::class.java.name), ListSubBazon, false)
        subBazons.register(ClassTypeReference(AbstractList::class.java.name), ListSubBazon, false)
        subBazons.register(ClassTypeReference(ArrayList::class.java.name), ListSubBazon, false)
        subBazons.register(ClassTypeReference(Map::class.java.name), MapSubBazon, false)
        subBazons.register(ClassTypeReference(AbstractMap::class.java.name), MapSubBazon, false)
        subBazons.register(ClassTypeReference(HashMap::class.java.name), MapSubBazon, false)
        subBazons.register(ClassTypeReference("java.lang.Byte"), ByteSubBazon, false)
        subBazons.register(ClassTypeReference("java.lang.Short"), ShortSubBazon, false)
        subBazons.register(ClassTypeReference("java.lang.Integer"), IntegerSubBazon, false)
        subBazons.register(ClassTypeReference("java.lang.Long"), LongSubBazon, false)
        subBazons.register(ClassTypeReference("java.lang.Boolean"), BooleanSubBazon, false)
        subBazons.register(ClassTypeReference("java.lang.Char"), CharSubBazon, false)
        subBazons.register(ClassTypeReference("java.lang.Double"), DoubleSubBazon, false)
        subBazons.register(ClassTypeReference("java.lang.Float"), FloatSubBazon, false)
        subBazons.register(ClassTypeReference("java.lang.String"), StringSubBazon, false)
    }

    inline fun tryWithRethrow(msg: String? = null, body: () -> Unit) {
        try {
            body()
        } catch (e: Throwable) {
            throw RuntimeException(msg, e)
        }
    }

    fun deserialize(mv: LocalVariablesSorter, reader: Reader, clazz: ClassTypeReference) {
        val stack = BytecodeStack()
        stack.putValue(clazz)
        val appender = SimpleAppender(mv, stack)
        deserialize(appender, reader, clazz, false)
    }

    fun deserialize(appender: BytecodeAppender, reader: Reader, clazz: ClassTypeReference, useReflection: Boolean) {
        tryWithRethrow("Exception while deserializing") {
            val model = modelFactory.makeModel(clazz, null)
            val context = DeserializationContext(this, appender, modelFactory, reader, useReflection)
            context.autoDeserialize(model)
        }
    }

    fun serialize(mv: LocalVariablesSorter, writer: Writer, clazz: ClassTypeReference, useReflection: Boolean) {
        val stack = BytecodeStack()
        stack.putValue(clazz)
        serialize(SimpleAppender(mv, stack), writer, clazz, useReflection)
    }

    fun serialize(appender: BytecodeAppender, writer: Writer, clazz: ClassTypeReference, useReflection: Boolean) {
        tryWithRethrow("Exception while serializing") {
            val model = modelFactory.makeModel(clazz, null)
            val context = SerializationContext(this, appender, modelFactory, writer, useReflection)
            context.autoSerialize(model)
        }
    }

}