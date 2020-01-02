package net.lessqq.amidstforge;

import amidst.*;
import amidst.mojangapi.minecraftinterface.MinecraftInterface;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProviderType;
import net.minecraft.world.biome.provider.OverworldBiomeProvider;
import net.minecraft.world.biome.provider.OverworldBiomeProviderSettings;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.OverworldGenSettings;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.event.world.WorldEvent.Unload;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.prefs.Preferences;

@EventBusSubscriber(modid = AmidstForgeMod.MOD_ID)
public class UIEventHandler {

    private Button amidstButton;
    private Button overworldButton;
    private Application application;
    private boolean serverRunning = false;

    @SubscribeEvent
    public void initGui(InitGuiEvent.Post evt) {
        Screen gui = evt.getGui();
        if (gui instanceof OptionsScreen) {
            int maxy = 0;
            for (Widget b : evt.getWidgetList()) {
                maxy = Math.max(b.y, maxy);
            }
            amidstButton = new Button(2, 2, 98, 20, I18n.format("amidstforge.ui.button.label"), (btn) -> startAmidstIntegrated());
            amidstButton.active = isBiomeProviderSane();
            overworldButton = new Button(gui.width - 100, 2, 98, 20,
                    I18n.format("amidstforge.ui.overworldbutton.label"), (btn) -> startAmidstOverworld(server));
            overworldButton.active = serverRunning;
            evt.addWidget(amidstButton);
            evt.addWidget(overworldButton);
        }
    }

    private boolean isBiomeProviderSane() {
        WorldSettings settings = new WorldSettings(1L, GameType.CREATIVE, true, false, WorldType.DEFAULT);
        OverworldBiomeProviderSettings biomeProviderSettings = BiomeProviderType.VANILLA_LAYERED.createSettings().setWorldInfo(new WorldInfo(settings, "AMIDSTWorld")).setGeneratorSettings(new OverworldGenSettings());
        OverworldBiomeProvider provider = BiomeProviderType.VANILLA_LAYERED.create(biomeProviderSettings);
        ForgeRegistry<Biome> biomeRegistry = (ForgeRegistry<Biome>) ForgeRegistries.BIOMES;

        Biome[] sample = provider.getBiomes(0, 0, 256, 256, false);
        for (Biome biome : sample) {
            int id = biomeRegistry.getID(biome);
            if (id < 0 || id > 255)
                return false;
        }
        return true;
    }

    private MinecraftServer server;

    @SubscribeEvent
    public void onServerStarted(FMLServerStartedEvent evt) {
        server = evt.getServer();
    }

    @SubscribeEvent
    public void onWorldLoad(Load evt) {
        ServerWorld overworld = null;
        if (server != null) {
            overworld = DimensionManager.getWorld(server, DimensionType.OVERWORLD, false, false);
        }
        if (overworld != null && overworld.getDimension().getType() == DimensionType.OVERWORLD)
            serverRunning = true;
        if (overworldButton != null)
            overworldButton.active = serverRunning;
    }

    @SubscribeEvent
    public void onWorldUnload(Unload evt) {
        ServerWorld overworld = null;
        if (server != null) {
            overworld = DimensionManager.getWorld(server, DimensionType.OVERWORLD, false, false);
        }
        if (overworld == null || overworld == evt.getWorld())
            serverRunning = false;
        if (overworldButton != null)
            overworldButton.active = serverRunning;
    }

    @SubscribeEvent
    public void postEvent(GuiScreenEvent.ActionPerformedEvent.Post evt) {
        if (evt.getGui() instanceof OptionsScreen && evt.getButton() == amidstButton) {
            startAmidstIntegrated();
        } else if (evt.getGui() instanceof OptionsScreen && evt.getButton() == overworldButton) {
            startAmidstOverworld(server);
        }
    }

    private void startAmidstOverworld(MinecraftServer server) {
        startAmidst(new OverworldMinecraftInterface(server));
    }

    private void startAmidstIntegrated() {
        startAmidst(new IntegratedMinecraftInterface());
    }

    public synchronized void startAmidst(MinecraftInterface minecraftInterface) {
        if (application != null) {
            Object mainWindow = getMainWindow(application);
            if (mainWindow != null)
                return;
            application = null;
        }
        System.setProperty("java.awt.headless", "false");
        SwingUtilities.invokeLater(() -> {
        try {
            PerApplicationInjector injector = new PerApplicationInjector(minecraftInterface,
                    AmidstMetaData.from(ResourceLoader.getProperties("/amidst/metadata.properties"),
                            ResourceLoader.getImage("/amidst/icon/amidst-16x16.png"),
                            ResourceLoader.getImage("/amidst/icon/amidst-32x32.png"),
                            ResourceLoader.getImage("/amidst/icon/amidst-48x48.png"),
                            ResourceLoader.getImage("/amidst/icon/amidst-64x64.png"),
                            ResourceLoader.getImage("/amidst/icon/amidst-128x128.png"),
                            ResourceLoader.getImage("/amidst/icon/amidst-256x256.png")),
                    new AmidstSettings(Preferences.userNodeForPackage(Amidst.class)));
            application = injector.getApplication();
            application.run();
        } catch (Exception e) {
            throw new RuntimeException("failed to launch AMIDST", e);
        }
        })        ;

    }

    private Object getMainWindow(Application app) {
        Field field;
        Object mainWindow;
        try {
            field = Application.class.getDeclaredField("mainWindow");
            field.setAccessible(true);
            mainWindow = field.get(app);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException("Failed to check for Amidst instance", e);
        }
        return mainWindow;
    }

}
