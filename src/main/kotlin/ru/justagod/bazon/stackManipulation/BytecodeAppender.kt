package ru.justagod.bazon.stackManipulation

interface BytecodeAppender {

    fun append(instruction: ru.justagod.bazon.stackManipulation.BytecodeInstruction)

    fun append(instructionSet: ru.justagod.bazon.stackManipulation.InstructionSet) {
        instructionSet.instructions.forEach(this::append)
    }

    operator fun plusAssign(instruction: ru.justagod.bazon.stackManipulation.BytecodeInstruction) {
        append(instruction)
    }

    operator fun plusAssign(instructionSet: ru.justagod.bazon.stackManipulation.InstructionSet) {
        append(instructionSet)
    }

    fun makeSetBuilder(): ru.justagod.bazon.stackManipulation.InstructionSet.Builder
}