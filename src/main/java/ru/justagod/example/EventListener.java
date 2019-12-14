package ru.justagod.example;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class EventListener {

    @SubscribeEvent
    public void onEnter(@NotNull PlayerEvent.PlayerLoggedInEvent e) {
        ExampleMod.wrapper.sendTo(new HelloPacket("Hello my dear friend", UUID.randomUUID()), (EntityPlayerMP) e.player);
    }
}
