package ru.justagod.bazon.stackManipulation

import org.objectweb.asm.commons.LocalVariablesSorter

class SimpleAppender(private val mv: LocalVariablesSorter, private val stack: ru.justagod.bazon.stackManipulation.BytecodeStack) : ru.justagod.bazon.stackManipulation.BytecodeAppender {

    override fun append(instruction: ru.justagod.bazon.stackManipulation.BytecodeInstruction) {
        instruction.transformStack(stack)
        instruction.accept(mv)
    }

    override fun makeSetBuilder(): ru.justagod.bazon.stackManipulation.InstructionSet.Builder = ru.justagod.bazon.stackManipulation.InstructionSet.Builder(stack.copy())
}