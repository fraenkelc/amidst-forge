package net.lessqq.amidstforge;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Mod(AmidstForgeMod.MOD_ID)
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
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(AmidstForgeMod.this::stopServer));
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

    private void patchGRPC() {
        // we're running grpc against an older netty version. We need to adjust for this.

        try {
            Field field = Class.forName("io.grpc.netty.AbstractNettyHandler").getDeclaredField("GRACEFUL_SHUTDOWN_NO_TIMEOUT");
            field.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.setLong(field, 1000L);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set grpc fields", e);
        }

    }
}
