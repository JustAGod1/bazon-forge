package ru.justagod.serialization

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import ru.justagod.bazon.Bazon
import ru.justagod.bazon.data.DataInputFlowReader
import ru.justagod.bazon.data.DataOutputFlowWriter
import ru.justagod.bazon.stackManipulation.BytecodeUtils
import ru.justagod.bazon.stackManipulation.LoadVariableInstruction
import ru.justagod.bazon.stackManipulation.ReturnInstruction
import ru.justagod.bazon.utils.ReadAndWriteAbleSubBazon
import ru.justagod.example.PersistentUnit
import ru.justagod.model.ClassTypeReference
import ru.justagod.model.factory.ReflectionModelFactory
import java.io.File
import java.time.*

object PacketSerializationManager {
    private var counter = 0L
    private val cache = hashMapOf<Class<*>, PersistentInator>()
    private val bazon = Bazon(ReflectionModelFactory(PacketSerializationManager::class.java.classLoader))

    init {
        delegatedBazon(
                ClassTypeReference(NBTTagCompound::class),
                "writeNBTTagCompound", "readNBTTagCompound"
        )
        delegatedBazon(
                ClassTypeReference("java.util.UUID"),
                "writeUUID", "readUUID"
        )

        val javaTimeClasses = listOf(
                Duration::class.java,
                Instant::class.java,
                LocalDate::class.java,
                LocalDateTime::class.java,
                LocalTime::class.java,
                MonthDay::class.java,
                OffsetDateTime::class.java,
                OffsetTime::class.java,
                Period::class.java,
                Year::class.java,
                YearMonth::class.java,
                ZonedDateTime::class.java,
                ZoneId::class.java,
                ZoneOffset::class.java)

        for (clazz in javaTimeClasses) {
            delegatedBazon(
                    ClassTypeReference(clazz.name),
                    "write${clazz.simpleName}",
                    "read${clazz.simpleName}"
            )
        }


        bazon.subBazons.register(
                ClassTypeReference(PersistentUnit::class),
                ReadAndWriteAbleSubBazon, true
        )
    }

    private fun delegatedBazon(
            type: ClassTypeReference,
            writeMethod: String,
            readMethod: String,
            holder: ClassTypeReference = ClassTypeReference(Serializers::class)
    ) {
        bazon.subBazons.register(
                type,
                DelegatedSubBazon(type, holder, readMethod, writeMethod),
                false
        )
    }

    fun fetchPersistenceInator(clazz: Class<*>): PersistentInator {
        if (clazz in cache) return cache[clazz]!!

        val inator = generatePersistentInator(clazz)
        cache[clazz] = inator

        return inator
    }

    private fun generatePersistentInator(target: Class<*>): PersistentInator {
        val node = ClassNode()
        node.access = Opcodes.ACC_PUBLIC
        node.name = "PersistentInator$${target.simpleName}$$counter"
        node.superName = "java/lang/Object"
        node.interfaces = arrayListOf("ru/justagod/serialization/PersistentInator")
        node.version = Opcodes.V1_8

        counter++

        val targetDesc = Type.getType(target).internalName

        createBridge(
                targetDesc,
                node,
                "read",
                "(L$targetDesc;Ljava/io/DataInput;)V",
                "(Ljava/lang/Object;Ljava/io/DataInput;)V"
        )
        createBridge(
                targetDesc,
                node,
                "write",
                "(L$targetDesc;Ljava/io/DataOutput;)V",
                "(Ljava/lang/Object;Ljava/io/DataOutput;)V"
        )

        createReader(targetDesc, node)
        createWritter(targetDesc, node)
        createConstructor(node)

        val bytes = nodeToByteArray(node)

        val f = File("inator/dump/${node.name}.class")
        f.parentFile.mkdirs()
        f.writeBytes(bytes)

        return defineClass(bytes).newInstance() as PersistentInator

    }


    private val defineMethod = ClassLoader::class.java.getDeclaredMethod(
            "defineClass",
            String::class.java,
            ByteArray::class.java,
            Int::class.java,
            Int::class.java
    )
    init {
        defineMethod.isAccessible = true
    }
    private fun defineClass(bytes: ByteArray): Class<*> {
        val loader = PacketSerializationManager::class.java.classLoader

        return defineMethod.invoke(loader, null, bytes, 0, bytes.size) as Class<*>
    }


    private fun nodeToByteArray(node: ClassNode): ByteArray {
        val writer = ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)
        node.accept(writer)
        return writer.toByteArray()
    }

    private fun createConstructor(node: ClassNode) {
        val m = MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null)
        m.instructions.add(VarInsnNode(Opcodes.ALOAD, 0))
        m.instructions.add(MethodInsnNode(
                Opcodes.INVOKESPECIAL,
                "java/lang/Object",
                "<init>",
                "()V",
                false
        ))
        m.instructions.add(InsnNode(Opcodes.RETURN))
        node.methods.add(m)
    }

    private fun createWritter(targetDesc: String, node: ClassNode) {
        val appender = BytecodeUtils.makeMethod(
                node,
                "write",
                "(L$targetDesc;Ljava/io/DataOutput;)V",
                Opcodes.ACC_STATIC or Opcodes.ACC_PRIVATE,
                null,
                null
        )
        val ref = ClassTypeReference(targetDesc.replace('/', '.'))
        appender += LoadVariableInstruction(ref, 0)
        bazon.serialize(
                appender,
                DataOutputFlowWriter { 1 },
                ref,
                true
        )

        appender += ReturnInstruction
    }

    private fun createReader(targetDesc: String, node: ClassNode) {
        val appender = BytecodeUtils.makeMethod(
                node,
                "read",
                "(L$targetDesc;Ljava/io/DataInput;)V",
                Opcodes.ACC_STATIC or Opcodes.ACC_PRIVATE,
                null,
                null
        )
        val ref = ClassTypeReference(targetDesc.replace('/', '.'))
        appender += LoadVariableInstruction(ref, 0)
        bazon.deserialize(
                appender,
                DataInputFlowReader { 1 },
                ref,
                true
        )

        appender += ReturnInstruction
    }

    private fun createBridge(
            targetDesc: String,
            node: ClassNode,
            implName: String,
            implDesc: String,
            bridgeDesc: String
    ) {
        val bridge = MethodNode(
                Opcodes.ACC_PUBLIC,
                implName,
                bridgeDesc,
                null,
                null
        )
        bridge.instructions.add(VarInsnNode(Opcodes.ALOAD, 1))
        bridge.instructions.add(TypeInsnNode(Opcodes.CHECKCAST, targetDesc))
        bridge.instructions.add(VarInsnNode(Opcodes.ALOAD, 2))
        bridge.instructions.add(MethodInsnNode(
                Opcodes.INVOKESTATIC,
                node.name,
                implName,
                implDesc,
                false
        ))
        bridge.instructions.add(InsnNode(Opcodes.RETURN))
        if (node.methods == null) {
            node.methods = arrayListOf()
        }
        node.methods.add(bridge)
    }


}