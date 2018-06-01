package net.lessqq.amidstforge;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@Mod(modid = AmidstForgeMod.MOD_ID, name = "see mcmod.info", version = "see mcmod.info", acceptableRemoteVersions = "*", useMetadata = true)
public class AmidstForgeMod {
    public static final String MOD_ID = "amidst-forge";
    public static final int AMIDST_REMOTE_PORT = 21548;
    private static final Logger logger = LogManager.getLogger(MOD_ID);

    private UIEventHandler eventHandler;
    private AmidstInterfaceImpl amidstInterface;
    private Server server;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        amidstInterface = new AmidstInterfaceImpl();
        eventHandler = new UIEventHandler(amidstInterface);
        MinecraftForge.EVENT_BUS.register(eventHandler);
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent evt) {
        evt.registerServerCommand(new AmidstRunCommand(eventHandler));
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        startServer();
    }

    private void startServer() {
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                AmidstForgeMod.this.stopServer();
            }));
            server = ServerBuilder.forPort(AMIDST_REMOTE_PORT).addService(amidstInterface).build().start();
        } catch (IOException e) {
            logger.error("Failed to start amidst remote service", e);
        }
    }

    private void stopServer() {
        if (server != null) {
            server.shutdownNow();
            server = null;
        }
    }

}
