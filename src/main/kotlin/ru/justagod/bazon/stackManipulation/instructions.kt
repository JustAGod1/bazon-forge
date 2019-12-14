package ru.justagod.bazon.stackManipulation

import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import org.objectweb.asm.commons.LocalVariablesSorter
import ru.justagod.model.*
import ru.justagod.model.factory.ReflectionModelFactory
import kotlin.properties.Delegates

fun <T : TypeReference> Type.toReference() = ReflectionModelFactory.makeTypeReference(this) as T

sealed class BytecodeInstruction {

    abstract fun accept(mv: LocalVariablesSorter)

    abstract fun transformStack(stack: BytecodeStack)
}


object IntToBooleanInstruction : BytecodeInstruction() {
    override fun accept(mv: LocalVariablesSorter) {}

    override fun transformStack(stack: BytecodeStack) {
        stack -= PrimitiveTypeReference(PrimitiveKind.INT)
        stack += PrimitiveTypeReference(PrimitiveKind.BOOLEAN)
    }

}

class NewVariableInstruction(private val type: TypeReference) : BytecodeInstruction() {

    var index: Int by Delegates.notNull()
        private set

    override fun accept(mv: LocalVariablesSorter) {
        index = mv.newLocal(type.toASMType())
    }

    override fun transformStack(stack: BytecodeStack) {}

}

class LoadVariableInstruction(private val type: TypeReference, private val index: () -> Int) : BytecodeInstruction() {

    constructor(type: TypeReference, variable: NewVariableInstruction) : this(type, { variable.index })
    constructor(type: TypeReference, index: Int) : this(type, { index })

    override fun accept(mv: LocalVariablesSorter) {
        mv.visitVarInsn(type.toASMType().getOpcode(ILOAD), index())
    }

    override fun transformStack(stack: BytecodeStack) {
        stack.putValue(type)
    }
}

class PutVariableInstruction(private val type: TypeReference, private val index: () -> Int) : BytecodeInstruction() {

    constructor(type: TypeReference, variable: NewVariableInstruction) : this(type, { variable.index })
    constructor(type: TypeReference, index: Int) : this(type, { index })

    override fun accept(mv: LocalVariablesSorter) {
        mv.visitVarInsn(type.toASMType().getOpcode(ISTORE), index())
    }

    override fun transformStack(stack: BytecodeStack) {
        stack.removeValue(type)
    }
}

class IincVariableInstruction(private val amount: Int, private val index: () -> Int) : BytecodeInstruction() {

    constructor(amount: Int, variable: NewVariableInstruction) : this(amount, { variable.index })
    constructor(amount: Int, index: Int) : this(amount, { index })

    override fun accept(mv: LocalVariablesSorter) {
        mv.visitIincInsn(index(), amount)
    }

    override fun transformStack(stack: BytecodeStack) {
    }
}

class FieldGetInstruction(val owner: ClassTypeReference, val name: String, val type: TypeReference) : BytecodeInstruction() {
    override fun accept(mv: LocalVariablesSorter) {
        mv.visitFieldInsn(Opcodes.GETFIELD, owner.toASMType().internalName, name, type.toASMType().descriptor)
    }

    override fun transformStack(stack: BytecodeStack) {
        stack.removeValue(owner)
        stack.putValue(type)
    }

}
class FieldPutStaticInstruction(private val owner: ClassTypeReference, private val name: String, private val type: TypeReference) : BytecodeInstruction() {
    override fun accept(mv: LocalVariablesSorter) {
        mv.visitFieldInsn(Opcodes.PUTSTATIC, owner.toASMType().internalName, name, type.toASMType().descriptor)
    }

    override fun transformStack(stack: BytecodeStack) {
        stack.removeValue(type)
    }
}

class FieldPutInstruction(private val owner: ClassTypeReference, private val name: String, private val type: TypeReference) : BytecodeInstruction() {
    override fun accept(mv: LocalVariablesSorter) {
        mv.visitFieldInsn(Opcodes.PUTFIELD, owner.toASMType().internalName, name, type.toASMType().descriptor)
    }

    override fun transformStack(stack: BytecodeStack) {
        stack.removeValue(type)
        stack.removeValue(owner)
    }
}

class ArrayLengthInstruction(val type: ArrayTypeReference) : BytecodeInstruction() {
    override fun accept(mv: LocalVariablesSorter) {
        mv.visitInsn(Opcodes.ARRAYLENGTH)
    }

    override fun transformStack(stack: BytecodeStack) {
        stack.removeValue(type)
        stack.putValue(PrimitiveTypeReference(PrimitiveKind.INT))
    }

}

class DupInstruction(private val type: TypeReference) : BytecodeInstruction() {
    override fun accept(mv: LocalVariablesSorter) {
        mv.visitInsn(Opcodes.DUP)
    }

    override fun transformStack(stack: BytecodeStack) {
        stack.removeValue(type)
        stack.putValue(type)
        stack.putValue(type)
    }

}

object ByteToIntInstruction : BytecodeInstruction() {
    override fun accept(mv: LocalVariablesSorter) {}

    override fun transformStack(stack: BytecodeStack) {
        stack.removeValue(PrimitiveTypeReference(PrimitiveKind.BYTE))
        stack.putValue(PrimitiveTypeReference(PrimitiveKind.INT))
    }
}

object ShortToIntInstruction : BytecodeInstruction() {
    override fun accept(mv: LocalVariablesSorter) {}

    override fun transformStack(stack: BytecodeStack) {
        stack.removeValue(PrimitiveTypeReference(PrimitiveKind.SHORT))
        stack.putValue(PrimitiveTypeReference(PrimitiveKind.INT))
    }
}

object CharToIntInstruction : BytecodeInstruction() {
    override fun accept(mv: LocalVariablesSorter) {}

    override fun transformStack(stack: BytecodeStack) {
        stack.removeValue(PrimitiveTypeReference(PrimitiveKind.CHAR))
        stack.putValue(PrimitiveTypeReference(PrimitiveKind.INT))
    }

}

class ArrayVariableLoadInstruction(val type: ArrayTypeReference) : BytecodeInstruction() {
    override fun accept(mv: LocalVariablesSorter) {
        mv.visitInsn(type.arrayType.toASMType().getOpcode(IALOAD))
    }

    override fun transformStack(stack: BytecodeStack) {
        stack.removeValue(PrimitiveTypeReference(PrimitiveKind.INT))
        stack.removeValue(type)
        stack.putValue(type.arrayType)
    }

}

class ArrayVariableStoreInstruction(val type: ArrayTypeReference) : BytecodeInstruction() {
    override fun accept(mv: LocalVariablesSorter) {
        mv.visitInsn(type.arrayType.toASMType().getOpcode(IASTORE))
    }

    override fun transformStack(stack: BytecodeStack) {
        stack.removeValue(type.arrayType)
        stack.removeValue(PrimitiveTypeReference(PrimitiveKind.INT))
        stack.removeValue(type)
    }
}

class CastInstruction(val from: TypeReference, val to: TypeReference) : BytecodeInstruction() {
    override fun accept(mv: LocalVariablesSorter) {
        mv.visitTypeInsn(Opcodes.CHECKCAST, to.toASMType().internalName)
    }

    override fun transformStack(stack: BytecodeStack) {
        stack.removeValue(from)
        stack.putValue(to)
    }

}

class NewInstanceInstruction(
        private val type: ClassTypeReference
) : BytecodeInstruction() {
    override fun accept(mv: LocalVariablesSorter) {
        mv.visitTypeInsn(Opcodes.NEW, type.toASMType().internalName)
    }

    override fun transformStack(stack: BytecodeStack) {
        stack.putValue(type)
    }

}

class NewArrayInstruction(private val type: TypeReference) : BytecodeInstruction() {

    init {
        assert(type !is PrimitiveTypeReference)
    }

    override fun accept(mv: LocalVariablesSorter) {
        mv.visitTypeInsn(Opcodes.NEWARRAY, type.toASMType().internalName)
    }

    override fun transformStack(stack: BytecodeStack) {
        stack.removeValue(PrimitiveTypeReference(PrimitiveKind.INT))
        stack.putValue(ArrayTypeReference(type))
    }

}

class NewPrimitiveArrayInstruction(private val type: PrimitiveTypeReference) : BytecodeInstruction() {

    override fun accept(mv: LocalVariablesSorter) {
        mv.visitIntInsn(Opcodes.NEWARRAY, type.kind.arrayType)
    }

    override fun transformStack(stack: BytecodeStack) {
        stack.removeValue(PrimitiveTypeReference(PrimitiveKind.INT))
        stack.putValue(ArrayTypeReference(type))
    }

}

class PopInstruction(private val type: TypeReference) : BytecodeInstruction() {
    override fun accept(mv: LocalVariablesSorter) {
        mv.visitInsn(Opcodes.POP)
    }

    override fun transformStack(stack: BytecodeStack) {
        stack -= type
    }

}

class InvokeInstanceMethodInstruction(
        private val owner: TypeReference,
        private val name: String,
        private val desc: String,
        private val opcode: Int,
        private val itf: Boolean = opcode == Opcodes.INVOKEINTERFACE
) : BytecodeInstruction() {

    init {
        assert(opcode == INVOKESPECIAL || opcode == INVOKEVIRTUAL || opcode == INVOKEINTERFACE)
    }

    override fun accept(mv: LocalVariablesSorter) {
        mv.visitMethodInsn(opcode, owner.toASMType().internalName, name, desc, itf)
    }

    override fun transformStack(stack: BytecodeStack) {
        Type.getArgumentTypes(desc).reversed().forEach { stack.removeValue(it.toReference()) }
        stack.removeValue(owner)
        val returnType = Type.getReturnType(desc)
        if (returnType.sort != Type.VOID) stack.putValue(returnType.toReference())
    }
}

class LongLoadInstruction(private val value: Long) : BytecodeInstruction() {
    override fun accept(mv: LocalVariablesSorter) {
        mv.visitLdcInsn(value)
    }

    override fun transformStack(stack: BytecodeStack) {
        stack += PrimitiveTypeReference(PrimitiveKind.LONG)
    }
}

class TypeReturnInstruction(private val type: TypeReference) : BytecodeInstruction() {
    override fun accept(mv: LocalVariablesSorter) {
        val opcode = if (type is PrimitiveTypeReference) {
            type.toASMType().getOpcode(Opcodes.IRETURN)
        } else Opcodes.ARETURN
        mv.visitInsn(opcode)
    }

    override fun transformStack(stack: BytecodeStack) {
        stack -= type
    }

}

class InvokeStaticMethodInstruction(
        private val owner: TypeReference,
        private val name: String,
        private val desc: String,
        private val itf: Boolean = false
) : BytecodeInstruction() {

    override fun accept(mv: LocalVariablesSorter) {
        mv.visitMethodInsn(INVOKESTATIC, owner.toASMType().internalName, name, desc, itf)
    }

    override fun transformStack(stack: BytecodeStack) {
        Type.getArgumentTypes(desc).reversed().forEach { stack.removeValue(it.toReference()) }
        val returnType = Type.getReturnType(desc)
        if (returnType.sort != Type.VOID) stack.putValue(returnType.toReference())
    }
}

object ReturnInstruction : BytecodeInstruction() {
    override fun accept(mv: LocalVariablesSorter) {
        mv.visitInsn(Opcodes.RETURN)
    }

    override fun transformStack(stack: BytecodeStack) {}

}

class StringLoadInstruction(private val value: String) : BytecodeInstruction() {
    override fun accept(mv: LocalVariablesSorter) {
        mv.visitLdcInsn(value)
    }

    override fun transformStack(stack: BytecodeStack) {
        stack += ClassTypeReference(String::class)
    }

}

class IntLoadInstruction(private val value: Int) : BytecodeInstruction() {
    override fun accept(mv: LocalVariablesSorter) {
        when (value) {
            in -1..5 -> mv.visitInsn(Opcodes.ICONST_0 + value)
            in Byte.MIN_VALUE..Byte.MAX_VALUE -> mv.visitIntInsn(Opcodes.BIPUSH, value)
            in Short.MIN_VALUE..Short.MAX_VALUE -> mv.visitIntInsn(Opcodes.SIPUSH, value)
            else -> mv.visitLdcInsn(value)
        }
    }

    override fun transformStack(stack: BytecodeStack) {
        stack += PrimitiveTypeReference(PrimitiveKind.INT)
    }

}

class TypeLoadInstruction(private val type: ClassTypeReference) : BytecodeInstruction() {
    override fun accept(mv: LocalVariablesSorter) {
        mv.visitLdcInsn(type.toASMType())
    }

    override fun transformStack(stack: BytecodeStack) {
        stack += ClassTypeReference("java.lang.Class")
    }

}

class LabelInstruction() : BytecodeInstruction() {

    val label = Label()

    override fun accept(mv: LocalVariablesSorter) {
        mv.visitLabel(label)
    }

    override fun transformStack(stack: BytecodeStack) {}

}

class DoubleIfInstruction(
        private val type1: TypeReference,
        private val type2: TypeReference,
        private val opcode: Int,
        private val label: () -> Label
) : BytecodeInstruction() {

    constructor(type1: TypeReference, type2: TypeReference, opcode: Int, label: LabelInstruction) : this(type1, type2, opcode, { label.label })
    constructor(type1: TypeReference, type2: TypeReference, opcode: Int, label: Label) : this(type1, type2, opcode, { label })

    override fun accept(mv: LocalVariablesSorter) {
        mv.visitJumpInsn(opcode, label())
    }

    override fun transformStack(stack: BytecodeStack) {
        stack.removeValue(type1)
        stack.removeValue(type2)

    }
}

class SingleIfInstruction(
        private val type: TypeReference,
        private val opcode: Int,
        private val label: () -> Label
) : BytecodeInstruction() {

    constructor(type: TypeReference, opcode: Int, label: LabelInstruction) : this(type, opcode, { label.label })
    constructor(type: TypeReference, opcode: Int, label: Label) : this(type, opcode, { label })

    override fun accept(mv: LocalVariablesSorter) {
        mv.visitJumpInsn(opcode, label())
    }

    override fun transformStack(stack: BytecodeStack) {
        stack.removeValue(type)
    }

}

class GoToInstruction(private val label: () -> Label) : BytecodeInstruction() {

    constructor(label: LabelInstruction) : this({ label.label })
    constructor(label: Label) : this({ label })

    override fun accept(mv: LocalVariablesSorter) {
        mv.visitJumpInsn(Opcodes.GOTO, label())
    }

    override fun transformStack(stack: BytecodeStack) {}

}

class IntInsnInstruction(val opcode: Int) : BytecodeInstruction() {
    override fun accept(mv: LocalVariablesSorter) {
        mv.visitInsn(opcode)
    }

    override fun transformStack(stack: BytecodeStack) {
        stack.putValue(PrimitiveTypeReference(PrimitiveKind.INT))
    }

}

