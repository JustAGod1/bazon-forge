package ru.justagod.bazon.serialization

import ru.justagod.model.AbstractModel
import ru.justagod.model.TypeReference
import ru.justagod.bazon.stackManipulation.BytecodeAppender

interface SubBazon {

    fun serialize(model: TypeReference, appender: BytecodeAppender, context: ru.justagod.bazon.serialization.SerializationContext, parent: AbstractModel)

    fun deserialize(model: TypeReference, appender: BytecodeAppender, context: ru.justagod.bazon.serialization.DeserializationContext, parent: AbstractModel)

}