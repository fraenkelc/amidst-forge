package net.lessqq.amidstforge;

import java.util.Random;

import amidst.mojangapi.world.biome.BiomeColor;
import amidst.mojangapi.world.biome.BiomeType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

@Mod(modid = AmidstForgeMod.MOD_ID, name = AmidstForgeMod.NAME, version = AmidstForgeMod.VERSION, acceptableRemoteVersions = "*", useMetadata = true)
public class AmidstForgeMod {
  public static final String MOD_ID = "amidst-forge";
  public static final String NAME = "Amidst for Forge";
  public static final String VERSION = "0.1";
  private UIEventHandler eventHandler;

  @EventHandler
  public void preInit(FMLPreInitializationEvent event) {
    eventHandler = new UIEventHandler();
    MinecraftForge.EVENT_BUS.register(eventHandler);
  }

  @EventHandler
  public void postInit(FMLPostInitializationEvent event) {
    updateBiomeList(new Random(12354L));
  }

  @EventHandler
  public void serverStarting(FMLServerStartingEvent evt) {
    evt.registerServerCommand(new AmidstRunCommand(eventHandler));
  }

  private void updateBiomeList(Random random) {
    ForgeRegistries.BIOMES.forEach(b -> maybeAddBiome(random, b, Biome.REGISTRY.getIDForObject(b)));
  }

  private void maybeAddBiome(Random random, Biome b, int idx) {

    amidst.mojangapi.world.biome.Biome biome = amidst.mojangapi.world.biome.Biome.getByName(b.getBiomeName());

    if (biome == null) {
      biome = new amidst.mojangapi.world.biome.Biome(b.getBiomeName(), idx,
          BiomeColor.from(random.nextInt(255), random.nextInt(255), random.nextInt(255)), BiomeType.OCEAN);
    }

  }
}
