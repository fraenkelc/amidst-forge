package net.lessqq.amidstforge;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.event.world.WorldEvent.Unload;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@EventBusSubscriber(modid = AmidstForgeMod.MOD_ID)
public class UIEventHandler {
    private static final Logger logger = LogManager.getLogger(AmidstForgeMod.MOD_ID);

    private GuiButton amidstButton;
    private GuiButton overworldButton;
    private boolean serverRunning = false;
    private Process amidstProcess;
    private final AmidstInterfaceImpl amidstInterface;

    public UIEventHandler(AmidstInterfaceImpl amidstInterface) {
        this.amidstInterface = amidstInterface;
    }

    @SubscribeEvent
    public void initGui(InitGuiEvent.Post evt) {
        GuiScreen gui = evt.getGui();
        if (gui instanceof GuiOptions) {
            int maxy = 0;
            for (GuiButton b : evt.getButtonList()) {
                maxy = b.y > maxy ? b.y : maxy;
            }
            amidstButton = new GuiButton(Integer.MIN_VALUE, 2, 2, 98, 20, I18n.format("amidstforge.ui.button.label"));
            amidstButton.enabled = isBiomeProviderSane();
            overworldButton = new GuiButton(Integer.MIN_VALUE, gui.width - 100, 2, 98, 20,
                    I18n.format("amidstforge.ui.overworldbutton.label"));
            overworldButton.enabled = serverRunning;
            evt.getButtonList().add(amidstButton);
            evt.getButtonList().add(overworldButton);
        }
    }

    private boolean isBiomeProviderSane() {
        WorldSettings settings = new WorldSettings(1L, GameType.CREATIVE, true, false, WorldType.DEFAULT);
        BiomeProvider provider = new BiomeProvider(new WorldInfo(settings, "AMIDSTWorld"));

        int[] ints = provider.biomeIndexLayer.getInts(0, 0, 256, 256);
        for (int i = 0; i < ints.length; i++)
            if (ints[i] < 0 || ints[i] > 255)
                return false;
        return true;
    }

    @SubscribeEvent
    public void onWorldLoad(Load evt) {
        WorldServer overworld = DimensionManager.getWorld(0);
        if (overworld != null && overworld == evt.getWorld())
            serverRunning = true;
        if (overworldButton != null)
            overworldButton.enabled = serverRunning;
    }

    @SubscribeEvent
    public void onWorldUnload(Unload evt) {
        WorldServer overworld = DimensionManager.getWorld(0);
        if (overworld == null || overworld == evt.getWorld())
            serverRunning = false;
        if (overworldButton != null)
            overworldButton.enabled = serverRunning;
    }

    @SubscribeEvent
    public void postEvent(GuiScreenEvent.ActionPerformedEvent.Post evt) {
        if (evt.getGui() instanceof GuiOptions && evt.getButton() == amidstButton) {
            startAmidst(new IntegratedBiomeProviderAccess());
        } else if (evt.getGui() instanceof GuiOptions && evt.getButton() == overworldButton) {
            startAmidst(new OverworldBiomeProviderAccess());
        }
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
            pb.command(System.getProperty("java.home") + "/bin/java", "-jar", amidstJar.toAbsolutePath().toString(), "-remote", AmidstForgeMod.INSTANCE.context.aeronDirectoryName());
            amidstProcess = pb.start();
        } catch (IOException e) {
            logger.error("Failed to start Amidst process", e);
        }
    }

    private Path extractAmidstJar() throws IOException {
        Path amidstJar = File.createTempFile("amidst", ".jar").toPath();
        try (InputStream is = AmidstForgeMod.class.getResourceAsStream("/amidst-forge_amidst.jar")) {
            Files.copy(is, amidstJar, StandardCopyOption.REPLACE_EXISTING);
        }
        return amidstJar;
    }
}
