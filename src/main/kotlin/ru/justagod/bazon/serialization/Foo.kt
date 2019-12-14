package ru.justagod.bazon.serialization

import java.io.DataInputStream
import java.io.DataOutputStream

class Foo {
    var a: Int = 5
    var b: Int = 6
    var bar = ru.justagod.bazon.serialization.Bar<Int>()

    fun write(out: DataOutputStream) {}

    fun read(input: DataInputStream) {}
}