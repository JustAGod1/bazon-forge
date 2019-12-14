package ru.justagod.mincer.util

import ru.justagod.mincer.control.MincerControlPane
import ru.justagod.mincer.control.MincerFS
import java.io.File

object MincerUtils {

    fun processFolder(panel: MincerControlPane, folder: File) {
        do {
            for (f in folder.walkTopDown().filter { it.path.endsWith(".class") && it.isFile }) {
                val name = f.absoluteFile.relativeTo(folder.absoluteFile)
                val lastModified = f.lastModified()
                val content = f.readBytes()

                panel.advance(content, name.path, lastModified).onModification {
                    f.writeBytes(it)
                }
            }
        } while (panel.endIteration())
    }


    private fun processRoot(root: File, mincer: MincerControlPane) {
        MincerUtils.processFolder(mincer, root)
    }
}