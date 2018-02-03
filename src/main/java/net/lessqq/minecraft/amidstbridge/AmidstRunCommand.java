package net.lessqq.minecraft.amidstbridge;

import java.util.Random;
import java.util.prefs.Preferences;

import amidst.Amidst;
import amidst.AmidstMetaData;
import amidst.AmidstSettings;
import amidst.PerApplicationInjector;
import amidst.ResourceLoader;
import amidst.mojangapi.world.biome.BiomeColor;
import amidst.mojangapi.world.biome.BiomeType;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.biome.Biome;

public class AmidstRunCommand extends CommandBase {

  @Override
  public String getName() {
    return "run";
  }

  @Override
  public String getUsage(ICommandSender sender) {
    return "amidstbridge.command.run.usage";
  }

  @Override
  public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
    try {
      updateBiomeList(new Random(12345l));

      PerApplicationInjector injector = new PerApplicationInjector(new IntegratedMinecraftInstance(),
          AmidstMetaData.from(ResourceLoader.getProperties("/amidst/metadata.properties"),
              ResourceLoader.getImage("/amidst/icon/amidst-16x16.png"),
              ResourceLoader.getImage("/amidst/icon/amidst-32x32.png"),
              ResourceLoader.getImage("/amidst/icon/amidst-48x48.png"),
              ResourceLoader.getImage("/amidst/icon/amidst-64x64.png"),
              ResourceLoader.getImage("/amidst/icon/amidst-128x128.png"),
              ResourceLoader.getImage("/amidst/icon/amidst-256x256.png")),
          new AmidstSettings(Preferences.userNodeForPackage(Amidst.class)));
      injector.getApplication().run();
    } catch (Exception e) {
      throw new RuntimeException("failed to launch AMIDST", e);
    }

  }

  private void updateBiomeList(Random random) {
    Biome.REGISTRY.forEach(b -> maybeAddBiome(random, b, Biome.REGISTRY.getIDForObject(b)));
  }

  private void maybeAddBiome(Random random, Biome b, int idx) {

    amidst.mojangapi.world.biome.Biome biome = amidst.mojangapi.world.biome.Biome.getByName(b.getBiomeName());

    if (biome == null) {
      biome = new amidst.mojangapi.world.biome.Biome(b.getBiomeName(), idx, BiomeColor.from(random.nextInt(255), random.nextInt(255), random.nextInt(255)),
          BiomeType.OCEAN);
    }

  }

}
