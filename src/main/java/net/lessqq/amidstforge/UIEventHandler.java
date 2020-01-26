package net.lessqq.amidstforge;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.prefs.Preferences;

@EventBusSubscriber(modid = AmidstForgeMod.MOD_ID)
public class UIEventHandler {
    private static final Logger logger = LogManager.getLogger(AmidstForgeMod.MOD_ID);

    private Button amidstButton;
    private Button overworldButton;
    private boolean serverRunning = false;
    private Process amidstProcess;
    private final AmidstInterfaceImpl amidstInterface;

    public UIEventHandler(AmidstInterfaceImpl amidstInterface) {
        this.amidstInterface = amidstInterface;
    }

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
        startAmidst(new OverworldBiomeProviderAccess(server));
    }

    private void startAmidstIntegrated() {
        startAmidst(new IntegratedBiomeProviderAccess());
    }

    public synchronized void startAmidst(BiomeProviderAccess biomeProviderAccess) {
        amidstInterface.setBiomeProviderAccess(biomeProviderAccess);
        if (amidstProcess != null) {
            if (amidstProcess.isAlive()) {
                return;
            }
            amidstProcess = null;
        }

        try {
            Path amidstJar = extractAmidstJar();
            ProcessBuilder pb = new ProcessBuilder();
            pb.inheritIO();
            pb.command(System.getProperty("java.home") + "/bin/java", "-jar", amidstJar.toAbsolutePath().toString(), "-remote", String.valueOf(AmidstForgeMod.AMIDST_REMOTE_PORT));
            amidstProcess = pb.start();
        } catch (IOException e) {
            logger.error("Failed to start Amidst process", e);
        }
    }

    private Path extractAmidstJar() throws IOException {
        Path amidstJar = File.createTempFile("amidst", ".jar").toPath();
        try (InputStream is = AmidstForgeMod.class.getResourceAsStream("/amidst-forge_amidst.jar.zip")) {
            Files.copy(is, amidstJar, StandardCopyOption.REPLACE_EXISTING);
        }
        return amidstJar;
    }
}
