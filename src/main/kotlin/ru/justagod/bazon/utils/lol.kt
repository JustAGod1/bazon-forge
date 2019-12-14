package ru.justagod.bazon.utils

import org.intellij.lang.annotations.Language

fun String.splitOrDefault(@Language("RegExp") regex: String): List<String> {
    val tmp = this.split(regex.toRegex())
    return if (tmp.isEmpty()) listOf(this)
    else tmp
}

operator fun StringBuilder.plusAssign(value: Any) {
    this.append(value)
}