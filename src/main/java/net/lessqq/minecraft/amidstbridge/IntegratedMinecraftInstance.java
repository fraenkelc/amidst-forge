package net.lessqq.minecraft.amidstbridge;

import amidst.mojangapi.minecraftinterface.MinecraftInterface;
import amidst.mojangapi.minecraftinterface.MinecraftInterfaceException;
import amidst.mojangapi.minecraftinterface.RecognisedVersion;
import amidst.mojangapi.world.WorldType;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;
import net.minecraft.world.storage.WorldInfo;

public class IntegratedMinecraftInstance implements MinecraftInterface {

  private BiomeProvider biomeProvider;

  @Override
  public void createWorld(long seed, WorldType worldType, String generatorOptions) throws MinecraftInterfaceException {
    WorldSettings settings = new WorldSettings(seed, GameType.CREATIVE, true, false, getWorldType(worldType));
    settings.setGeneratorOptions(generatorOptions);
    biomeProvider = new BiomeProvider(new WorldInfo(settings, "AMIDSTWorld"));
  }

  private net.minecraft.world.WorldType getWorldType(WorldType worldType) throws MinecraftInterfaceException {
    switch (worldType) {
    case AMPLIFIED:
      return net.minecraft.world.WorldType.AMPLIFIED;
    case CUSTOMIZED:
      return net.minecraft.world.WorldType.CUSTOMIZED;
    case DEFAULT:
      return net.minecraft.world.WorldType.DEFAULT;
    case FLAT:
      return net.minecraft.world.WorldType.FLAT;
    case LARGE_BIOMES:
      return net.minecraft.world.WorldType.LARGE_BIOMES;
    }
    throw new MinecraftInterfaceException("Unknown World type '" + worldType + "'");
  }

  @Override
  public int[] getBiomeData(int x, int y, int width, int height, boolean useQuarterResolution)
      throws MinecraftInterfaceException {
    IntCache.resetIntCache();
    GenLayer layer;
    if (useQuarterResolution)
      layer = biomeProvider.genBiomes;
    else
      layer = biomeProvider.biomeIndexLayer;
    return layer.getInts(x, y, width, height).clone();
  }

  @Override
  public RecognisedVersion getRecognisedVersion() {
    return RecognisedVersion.UNKNOWN;
  }

}
