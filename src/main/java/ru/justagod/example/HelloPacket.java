package ru.justagod.example;

import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

import java.util.UUID;

public class HelloPacket extends PacketBase{

    public String msg;
    public SomeDataCompound a = new SomeDataCompound(2, 3, 4);
    public final UUID uuid;

    public HelloPacket() {
        uuid = new UUID(0, 0);
    }

    public HelloPacket(String msg, UUID uuid) {
        this.msg = msg;
        this.uuid = uuid;
    }

    @Override
    protected void handle(MessageContext ctx) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(msg));
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(uuid.toString()));
    }
}
