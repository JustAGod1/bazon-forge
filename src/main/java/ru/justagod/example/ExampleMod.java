package ru.justagod.example;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = "example")
public class ExampleMod {

    public static final SimpleNetworkWrapper wrapper = NetworkRegistry.INSTANCE.newSimpleChannel("example");

    @Mod.EventHandler
    public void init(FMLPreInitializationEvent e) {
        wrapper.registerMessage(new HelloPacket(), HelloPacket.class, 0, Side.CLIENT);
        MinecraftForge.EVENT_BUS.register(new EventListener());
        FMLCommonHandler.instance().bus().register(new EventListener());
    }

}
