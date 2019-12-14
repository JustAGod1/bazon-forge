package ru.justagod.mincer

import ru.justagod.mincer.control.MincerCachedFS
import ru.justagod.mincer.control.MincerControlPane
import ru.justagod.mincer.control.MincerFS
import ru.justagod.mincer.control.MincerResult
import ru.justagod.mincer.pipeline.Pipeline
import ru.justagod.mincer.processor.SubMincer
import ru.justagod.mincer.util.NodesFactory
import ru.justagod.model.InheritanceHelper
import ru.justagod.model.factory.BytecodeModelFactory
import ru.justagod.model.factory.CachedFactory
import ru.justagod.model.factory.FallbackModelFactory
import java.io.File
import java.util.*

class Mincer private constructor(
        val fs: MincerFS,
        val canSkip: Boolean,
        pipelines: List<Pipeline<*, *>>
) : MincerControlPane {
    val nodes = NodesFactory { fs.pullClass(it) ?: throw BytecodeModelFactory.BytecodeNotFoundException(it) }
    val factory = CachedFactory(FallbackModelFactory(this.javaClass.classLoader, nodes))
    val inheritance = InheritanceHelper(factory)

    private var queues = pipelines.map { it.unwind(fs) }
    private val archive = hashMapOf<String, MutableList<String>>()

    init {
        queues.forEach {
            (it.pipeline.worker as SubMincer<Unit, Any>)
                    .startProcessing(
                            Unit,
                            fs.pullArchive(it.pipeline.id),
                            inheritance,
                            it.pipeline as Pipeline<Unit, Any>
                    )
        }
    }

    override fun advance(source: ByteArray, name: String, lastModified: Long): MincerResult {
        return queues.fold(MincerResult(source, false)) { acc, head ->
            acc merge head.process(this, acc.resultedBytecode, lastModified, name)
        }
    }

    override fun endIteration(): Boolean {
        for ((id, entries) in archive) {
            fs.pushArchive(id, entries)
        }
        archive.clear()

        queues.forEach {
            (it.pipeline.worker as SubMincer<Any, Any>).endProcessing(
                    it.input,
                    it.archive,
                    inheritance,
                    it.pipeline as Pipeline<Any, Any>
            )
        }
        queues = queues.mapNotNull { it.advance(it.pipeline.value!!, fs, inheritance) }
        return queues.isNotEmpty()
    }

    fun submitArchiveEntry(id: String, name: String) {
        archive.computeIfAbsent(id) { arrayListOf() } += name
    }

    class Builder(private val fs: MincerFS, private val canSkip: Boolean) {
        private val registry = LinkedList<Pipeline<*, *>>()

        fun registerSubMincer(pipeline: Pipeline<*, *>): Builder {
            registry += pipeline
            return this
        }

        fun build(): MincerControlPane = Mincer(MincerCachedFS(fs), canSkip, registry)
    }
}