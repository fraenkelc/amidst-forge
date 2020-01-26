package net.lessqq.amidstforge;

import amidst.remote.AmidstRemoteServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

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

        URLClassLoader fixedClassLoader;
        try {
            // Service discovery in forge is broken, see
            // https://github.com/MinecraftForge/MinecraftForge/issues/6029
            // and https://github.com/cpw/modlauncher/issues/39
            fixedClassLoader = new URLClassLoader(new URL[]{FMLLoader.getLoadingModList().getModFileById(AmidstForgeMod.MOD_ID).getFile().getFilePath().toUri().toURL()}, AmidstForgeMod.class.getClassLoader());
        } catch (MalformedURLException e) {
            throw new UncheckedIOException("failed to build mod url", e);
        }
        amidstRemoteServer = new AmidstRemoteServer(AMIDST_REMOTE_PORT, amidstInterface, fixedClassLoader);
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
