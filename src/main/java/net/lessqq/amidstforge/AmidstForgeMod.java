package net.lessqq.amidstforge;

import io.grpc.CompressorRegistry;
import io.grpc.DecompressorRegistry;
import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

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
            Configurator.setLevel("io.grpc.netty.shaded.io.grpc.netty.NettyServerHandler", Level.WARN);
            server = NettyServerBuilder
                    .forAddress(new InetSocketAddress(InetAddress.getLoopbackAddress(), AMIDST_REMOTE_PORT))
                    .addService(amidstInterface)
                    .compressorRegistry(CompressorRegistry.newEmptyInstance())
                    .decompressorRegistry(DecompressorRegistry.emptyInstance())
                    .build().start();
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
