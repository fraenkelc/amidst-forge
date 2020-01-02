package net.lessqq.amidstforge;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod(AmidstForgeMod.MOD_ID)
@Mod.EventBusSubscriber
public class AmidstForgeMod {
    public static final String MOD_ID = "amidst-forge";
    public static final String NAME = "Amidst for Forge";
    private UIEventHandler eventHandler;
    public static Logger LOGGER = LogManager.getLogger();

    public AmidstForgeMod() {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::preInit);

    }

    @SubscribeEvent
    public void preInit(FMLClientSetupEvent event) {
        eventHandler = new UIEventHandler();
        MinecraftForge.EVENT_BUS.register(eventHandler);
    }

    @SubscribeEvent
    public void serverStarting(FMLServerStartingEvent evt) {
        new AmidstRunCommand(eventHandler, evt.getCommandDispatcher());
    }

}
