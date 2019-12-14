package ru.justagod.bazon.serialization

import ru.justagod.model.ClassTypeReference
import ru.justagod.bazon.stackManipulation.BytecodeAppender

interface InstanceFactory {

    fun makeInstance(type: ClassTypeReference, appender: BytecodeAppender)

}