package ru.justagod.bazon.stackManipulation

import ru.justagod.model.ArrayTypeReference
import ru.justagod.model.ClassTypeReference
import ru.justagod.model.PrimitiveKind.INT
import ru.justagod.model.PrimitiveTypeReference
import ru.justagod.model.TypeReference
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.LocalVariablesSorter
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.MethodNode

object BytecodeUtils {

    fun makeFor(appender: ru.justagod.bazon.stackManipulation.BytecodeAppender,
                maximumFetcher: ru.justagod.bazon.stackManipulation.InstructionSet,
                body: (ru.justagod.bazon.stackManipulation.NewVariableInstruction) -> Unit
    ) {
        val variable = ru.justagod.bazon.stackManipulation.NewVariableInstruction(PrimitiveTypeReference(INT))
        appender += variable
        appender += ru.justagod.bazon.stackManipulation.IntInsnInstruction(Opcodes.ICONST_0)
        appender += ru.justagod.bazon.stackManipulation.PutVariableInstruction(PrimitiveTypeReference(INT), variable)
        val forStart = ru.justagod.bazon.stackManipulation.LabelInstruction()
        appender += forStart
        val forEnd = ru.justagod.bazon.stackManipulation.LabelInstruction()
        appender += ru.justagod.bazon.stackManipulation.LoadVariableInstruction(PrimitiveTypeReference(INT), variable)
        appender += maximumFetcher
        appender += ru.justagod.bazon.stackManipulation.DoubleIfInstruction(PrimitiveTypeReference(INT), PrimitiveTypeReference(INT), Opcodes.IF_ICMPGE, forEnd)
        body.invoke(variable)
        appender += ru.justagod.bazon.stackManipulation.IincVariableInstruction(1, variable)
        appender += ru.justagod.bazon.stackManipulation.GoToInstruction(forStart)
        appender += forEnd
    }

    fun makeArrayFor(type: ArrayTypeReference, appender: ru.justagod.bazon.stackManipulation.BytecodeAppender, body: ru.justagod.bazon.stackManipulation.BytecodeUtils.ArrayWorker.(ru.justagod.bazon.stackManipulation.NewVariableInstruction) -> Unit) {
        val variable = ru.justagod.bazon.stackManipulation.NewVariableInstruction(type)
        appender += variable
        appender += ru.justagod.bazon.stackManipulation.PutVariableInstruction(type, variable)
        val fetcher = ru.justagod.bazon.stackManipulation.InstructionSet.Companion.create(
                ru.justagod.bazon.stackManipulation.LoadVariableInstruction(type, variable),
                ru.justagod.bazon.stackManipulation.ArrayLengthInstruction(type)
        )
        val internalBody: (ru.justagod.bazon.stackManipulation.NewVariableInstruction) -> Unit = { i ->

            val worker = ru.justagod.bazon.stackManipulation.BytecodeUtils.ArrayWorker(variable, i, type)
            worker.body(i)
        }
        ru.justagod.bazon.stackManipulation.BytecodeUtils.makeFor(appender, fetcher, internalBody)
    }

    fun makeDefaultInstance(type: ClassTypeReference, appender: ru.justagod.bazon.stackManipulation.BytecodeAppender) {
        appender += ru.justagod.bazon.stackManipulation.NewInstanceInstruction(type)
        appender += ru.justagod.bazon.stackManipulation.DupInstruction(type)
        appender += ru.justagod.bazon.stackManipulation.InvokeInstanceMethodInstruction(type, "<init>", "()V", Opcodes.INVOKESPECIAL)
    }

    fun makeArray(type: TypeReference, appender: ru.justagod.bazon.stackManipulation.BytecodeAppender) {
        appender += if (type is PrimitiveTypeReference) {
            ru.justagod.bazon.stackManipulation.NewPrimitiveArrayInstruction(type)
        } else {
            ru.justagod.bazon.stackManipulation.NewArrayInstruction(type)
        }
    }

    fun makeMethod(node: ClassNode, name: String, desc: String, access: Int, signature: String? = null, exceptions: Array<String>? = null): ru.justagod.bazon.stackManipulation.BytecodeAppender {
        val method = MethodNode(Opcodes.ASM5, name, desc, signature, exceptions)
        method.access = access
        method.instructions = InsnList()
        val visitor = LocalVariablesSorter(access, desc, method)
        if (node.methods == null) {
            node.methods = mutableListOf()
        }
        node.methods.add(method)
        return ru.justagod.bazon.stackManipulation.SimpleAppender(visitor, ru.justagod.bazon.stackManipulation.BytecodeStack())
    }

    fun makeDefaultConstructor(node: ClassNode) {
        val appender = ru.justagod.bazon.stackManipulation.BytecodeUtils.makeMethod(node, "<init>", "()V", Opcodes.ACC_PUBLIC)
        appender += ru.justagod.bazon.stackManipulation.LabelInstruction()
        val superType = ClassTypeReference(node.superName.replace("[\\\\/]".toRegex(), "."))
        appender += ru.justagod.bazon.stackManipulation.LoadVariableInstruction(superType, 0)
        appender += ru.justagod.bazon.stackManipulation.InvokeInstanceMethodInstruction(superType, "<init>", "()V", Opcodes.INVOKESPECIAL)
        appender += ru.justagod.bazon.stackManipulation.LabelInstruction()
        appender += ru.justagod.bazon.stackManipulation.ReturnInstruction
        appender += ru.justagod.bazon.stackManipulation.LabelInstruction()
    }

    class ArrayWorker(private val arrVariable: ru.justagod.bazon.stackManipulation.NewVariableInstruction, private val iVariable: ru.justagod.bazon.stackManipulation.NewVariableInstruction, private val type: ArrayTypeReference) {
        fun putValue(appender: ru.justagod.bazon.stackManipulation.BytecodeAppender) {
            appender += ru.justagod.bazon.stackManipulation.LoadVariableInstruction(type, arrVariable)
            appender += ru.justagod.bazon.stackManipulation.LoadVariableInstruction(PrimitiveTypeReference(INT), iVariable)
            appender += ru.justagod.bazon.stackManipulation.ArrayVariableLoadInstruction(type)
        }
    }
}