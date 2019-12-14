package ru.justagod.example;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class ScaryPacket implements IMessage {

    private int a;
    private int b;
    private int c;
    private SomeDataCompound data;

    public ScaryPacket() {
    }

    public ScaryPacket(int a, int b, int c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        a = buf.readInt();
        b = buf.readInt();
        c = buf.readInt();
        data = new SomeDataCompound(buf.readInt(), buf.readInt(), buf.readInt());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(a);
        buf.writeInt(b);
        buf.writeInt(c);
        buf.writeInt(data.a);
        buf.writeInt(data.b);
        buf.writeInt(data.c);
    }


    public class SomeDataCompound {
        private final int c;
        public int b;
        private int a;

        public SomeDataCompound(int a, int b, int c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }
    }
}
