package net.lessqq.amidstforge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = AmidstForgeMod.MOD_ID, name = AmidstForgeMod.NAME, acceptableRemoteVersions = "*", useMetadata = true)
public class AmidstForgeMod {
    public static final String MOD_ID = "amidst-forge";
    public static final String NAME = "Amidst for Forge";
    private UIEventHandler eventHandler;
    public static Logger LOGGER = LogManager.getLogger();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        eventHandler = new UIEventHandler();
        MinecraftForge.EVENT_BUS.register(eventHandler);
        LOGGER = event.getModLog();
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent evt) {
        evt.registerServerCommand(new AmidstRunCommand(eventHandler));
    }

}
