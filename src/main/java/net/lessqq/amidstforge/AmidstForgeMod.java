package net.lessqq.amidstforge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.aeron.Aeron;
import io.aeron.Aeron.Context;
import io.aeron.ConcurrentPublication;
import io.aeron.Subscription;
import io.aeron.driver.MediaDriver;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = AmidstForgeMod.MOD_ID, name = "see mcmod.info", version = "see mcmod.info", acceptableRemoteVersions = "*", useMetadata = true)
public class AmidstForgeMod {
    public static final String MOD_ID = "amidst-forge";
    @Mod.Instance
    public static AmidstForgeMod INSTANCE;
    private static final Logger logger = LogManager.getLogger(MOD_ID);

    private UIEventHandler eventHandler;
    private AmidstInterfaceImpl amidstInterface;
    private MediaDriver driver;
    Context context;
    private Subscription subscription;
    private ConcurrentPublication publication;

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

    @SuppressWarnings("resource")
    private void startServer() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            AmidstForgeMod.this.stopServer();
        }));
        driver = MediaDriver.launchEmbedded();
        context = new Aeron.Context().aeronDirectoryName(driver.aeronDirectoryName());
        context.availableImageHandler(amidstInterface::onAvailableImage);
        final Aeron aeron = Aeron.connect(context);
        subscription = aeron.addSubscription("aeron:ipc", Constants.REQUEST_STREAM_ID);
        publication = aeron.addPublication("aeron:ipc", Constants.RESPONSE_STREAM_ID);
        amidstInterface.setPublication(publication);
        logger.info("Use the URL \"{}\" to connect with amidst.", driver.aeronDirectoryName());
    }

    private void stopServer() {
        if (subscription != null) {
            subscription.close();
        }
        if (publication != null) {
            publication.close();
        }
        if (context != null) {
            context.close();
        }
        if (driver != null) {
            driver.close();
        }
    }

}
