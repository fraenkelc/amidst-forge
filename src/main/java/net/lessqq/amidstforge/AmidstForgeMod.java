package net.lessqq.amidstforge;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
@Mod(AmidstForgeMod.MOD_ID)
@Mod.EventBusSubscriber
public class AmidstForgeMod {
    public static final String MOD_ID = "amidst-forge";
    public static final int AMIDST_REMOTE_PORT = 21548;
    private static final Logger logger = LogManager.getLogger(MOD_ID);

    private UIEventHandler eventHandler;
    private AmidstInterfaceImpl amidstInterface;
    private Server server;

    public AmidstForgeMod() {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::preInit);

    }

    @SubscribeEvent
    public void preInit(FMLClientSetupEvent event) {
        amidstInterface = new AmidstInterfaceImpl();
        eventHandler = new UIEventHandler(amidstInterface);
        MinecraftForge.EVENT_BUS.register(eventHandler);
    }

    @SubscribeEvent
    public void serverStarting(FMLServerStartingEvent evt) {
        new AmidstRunCommand(eventHandler, evt.getCommandDispatcher());
    }

    @SubscribeEvent
    public void postInit(FMLLoadCompleteEvent event) {
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
