package net.lessqq.amidstforge;

import amidst.mojangapi.minecraftinterface.MinecraftInterface;
import amidst.mojangapi.minecraftinterface.MinecraftInterfaceException;
import amidst.mojangapi.minecraftinterface.RecognisedVersion;
import amidst.mojangapi.world.WorldType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class OverworldMinecraftInterface implements MinecraftInterface {

  private BiomeProvider biomeProvider;

  @Override
  public void createWorld(long seed, WorldType worldType, String generatorOptions) throws MinecraftInterfaceException {
    MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
    if (server != null) {
      WorldServer world = server.getWorld(0);
      if (world != null) {
        biomeProvider = world.getBiomeProvider();
      }
    }
  }

  @Override
  public int[] getBiomeData(int x, int y, int width, int height, boolean useQuarterResolution)
      throws MinecraftInterfaceException {
    IntCache.resetIntCache();
    if (biomeProvider != null) {
      GenLayer layer;
      if (useQuarterResolution)
        layer = biomeProvider.genBiomes;
      else
        layer = biomeProvider.biomeIndexLayer;
      return layer.getInts(x, y, width, height).clone();
    }
    return new int[width*height];

  }

  @Override
  public RecognisedVersion getRecognisedVersion() {
    return RecognisedVersion.UNKNOWN;
  }

}
