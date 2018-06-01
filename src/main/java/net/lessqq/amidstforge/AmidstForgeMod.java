package net.lessqq.amidstforge;

import amidst.remote.AmidstRemoteServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(AmidstForgeMod.MOD_ID)
public class AmidstForgeMod {
    public static final String MOD_ID = "amidst-forge";
    public static final int AMIDST_REMOTE_PORT = 21548;
    private static final Logger logger = LogManager.getLogger(MOD_ID);

    private UIEventHandler eventHandler;
    private AmidstInterfaceImpl amidstInterface;
    private AmidstRemoteServer amidstRemoteServer;

    public AmidstForgeMod() {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::preInit);
        modEventBus.addListener(this::postInit);

    }

    public void preInit(FMLClientSetupEvent event) {
        amidstInterface = new AmidstInterfaceImpl();
        eventHandler = new UIEventHandler(amidstInterface);
        MinecraftForge.EVENT_BUS.register(eventHandler);
        MinecraftForge.EVENT_BUS.addListener(this::serverStarting);
    }

    public void serverStarting(FMLServerStartingEvent evt) {
        new AmidstRunCommand(eventHandler, evt.getCommandDispatcher());
    }

    public void postInit(InterModProcessEvent event) {
        startServer();
    }

    private void startServer() {
        amidstRemoteServer = new AmidstRemoteServer(AMIDST_REMOTE_PORT, amidstInterface);
        Runtime.getRuntime().addShutdownHook(new Thread(AmidstForgeMod.this::stopServer));
    }

    private void stopServer() {
        if (amidstRemoteServer != null) {
            try {
                amidstRemoteServer.close();
            } catch (Exception e) {
                logger.warn("Failed to close server cleanly", e);
            }
            amidstRemoteServer = null;
        }
    }
}
