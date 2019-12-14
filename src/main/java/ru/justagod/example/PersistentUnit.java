package ru.justagod.example;

import ru.justagod.serialization.PacketSerializationManager;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;

public class PersistentUnit {

    public void read(DataInput input) {
        Class<?> clazz = this.getClass();
        while (PacketBase.class.isAssignableFrom(clazz)) {
            PacketSerializationManager.INSTANCE.fetchPersistenceInator(clazz).read(this, input);
            clazz = clazz.getSuperclass();
        }
    }


    public void write(DataOutput output) {
        Class<?> clazz = this.getClass();
        while (PacketBase.class.isAssignableFrom(clazz)) {
            PacketSerializationManager.INSTANCE.fetchPersistenceInator(clazz).write(this, output);
            clazz = clazz.getSuperclass();
        }
    }

}
