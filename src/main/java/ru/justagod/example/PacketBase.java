package ru.justagod.example;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public abstract class PacketBase extends PersistentUnit implements IMessage, IMessageHandler<PacketBase, PacketBase> {
    @Override
    public void fromBytes(ByteBuf buf) {
        DataInputStream input = new DataInputStream(new ByteBufInputStream(buf));
        read(input);
    }

    @Override
    public final void toBytes(ByteBuf buf) {
        DataOutputStream output = new DataOutputStream(new ByteBufOutputStream(buf));
        write(output);
    }


    protected abstract void handle(MessageContext ctx);

    @Override
    public PacketBase onMessage(PacketBase message, MessageContext ctx) {
        message.handle(ctx);
        return null;
    }
}
