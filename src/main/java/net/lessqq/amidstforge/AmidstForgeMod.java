package net.lessqq.amidstforge;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = AmidstForgeMod.MOD_ID, name = AmidstForgeMod.NAME, version = AmidstForgeMod.VERSION, acceptableRemoteVersions = "*", useMetadata = true)
public class AmidstForgeMod {
    public static final String MOD_ID = "amidst-forge";
    public static final String NAME = "Amidst for Forge";
    public static final String VERSION = "0.1";
    private UIEventHandler eventHandler;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        eventHandler = new UIEventHandler();
        MinecraftForge.EVENT_BUS.register(eventHandler);
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent evt) {
        evt.registerServerCommand(new AmidstRunCommand(eventHandler));
    }

}
