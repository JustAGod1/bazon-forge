package ru.justagod.utils


import org.objectweb.asm.*
import org.objectweb.asm.commons.LocalVariablesSorter
import ru.justagod.bazon.stackManipulation.*
import ru.justagod.model.ClassTypeReference
import ru.justagod.model.OBJECT_REFERENCE
import java.lang.reflect.Field

object AsmUtil {

    fun makeForI(mv: LocalVariablesSorter, body: LocalVariablesSorter.(Int) -> Unit, max: LocalVariablesSorter.() -> Unit) {
        val variable = mv.newLocal(Type.getType("I"))
        mv.visitInsn(Opcodes.ICONST_0)
        mv.visitVarInsn(Opcodes.ISTORE, variable)
        val forStart = Label()
        val forEnd = Label()
        mv.visitLabel(forStart)
        mv.visitVarInsn(Opcodes.ILOAD, variable)
        mv.max()
        mv.visitJumpInsn(Opcodes.IF_ICMPGE, forEnd)
        mv.body(variable)
        mv.visitIincInsn(variable, 1)
        mv.visitJumpInsn(Opcodes.GOTO, forStart)
        mv.visitLabel(forEnd)
    }

    fun getSuperClass(bytecode: ByteArray): String? {
        var result: String? = null
        accept(bytecode, object : ClassVisitor(Opcodes.ASM5) {
            override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String>?) {

                result = superName
            }
        })
        return result
    }

    fun accept(bytecode: ByteArray, visitor: ClassVisitor, flags: Int = 0) {
        val reader = ClassReader(bytecode)
        reader.accept(visitor, flags)
    }

    private fun getFieldViaReflect(ref: ClassTypeReference, name: String, appender: BytecodeAppender) {
        appender += TypeLoadInstruction(ref)
        appender += StringLoadInstruction(name)
        appender += InvokeInstanceMethodInstruction(
                ClassTypeReference(Class::class),
                "getDeclaredField",
                "(Ljava/lang/String;)Ljava/lang/reflect/Field;",
                Opcodes.INVOKEVIRTUAL
        )
        appender += DupInstruction(ClassTypeReference(Field::class))
        appender += IntLoadInstruction(1)
        appender += IntToBooleanInstruction
        appender += InvokeInstanceMethodInstruction(
                ClassTypeReference(Field::class),
                "setAccessible",
                "(Z)V",
                Opcodes.INVOKEVIRTUAL
        )

    }

    fun setFieldValueViaReflect(ref: ClassTypeReference, name: String, appender: BytecodeAppender, value: InstructionSet) {
        getFieldViaReflect(ref, name, appender)
        appender += value

        appender += InvokeInstanceMethodInstruction(
                ClassTypeReference(Field::class),
                "set",
                "(Ljava/lang/Object;Ljava/lang/Object;)V",
                Opcodes.INVOKEVIRTUAL
        )
    }

    fun getFieldValueViaReflect(ref: ClassTypeReference, name: String, appender: BytecodeAppender, value: InstructionSet) {
        getFieldViaReflect(ref, name, appender)
        appender += value

        appender += InvokeInstanceMethodInstruction(
                ClassTypeReference(Field::class),
                "get",
                "(Ljava/lang/Object;)Ljava/lang/Object;",
                Opcodes.INVOKEVIRTUAL
        )
    }
}



fun Type.isArray() = this.descriptor.startsWith("[")

val Type.arrayType: Type?
    get() = if (isArray()) Type.getType(this.descriptor.substring(1)) else null