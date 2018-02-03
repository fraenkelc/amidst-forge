package net.lessqq.minecraft.amidstbridge;

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

@Mod(modid = AmidstBridgeMod.MOD_ID, name = AmidstBridgeMod.NAME, version = AmidstBridgeMod.VERSION, acceptableRemoteVersions = "*", clientSideOnly = true)
public class AmidstBridgeMod {
  public static final String MOD_ID = "amidstbridge";
  public static final String NAME = "AMIDST bridge mod";
  public static final String VERSION = "0.1";

  @EventHandler
  public void preInit(FMLPreInitializationEvent event) {
    MinecraftForge.EVENT_BUS.register(new UIEventHandler());
  }

  @EventHandler
  public void postInit(FMLPostInitializationEvent event) {
    updateBiomeList(new Random(12354L));
  }

  @EventHandler
  public void serverStarting(FMLServerStartingEvent evt) {
    evt.registerServerCommand(new AmidstRunCommand());
  }

  private void updateBiomeList(Random random) {
    Biome.REGISTRY.forEach(b -> maybeAddBiome(random, b, Biome.REGISTRY.getIDForObject(b)));
  }

  private void maybeAddBiome(Random random, Biome b, int idx) {

    amidst.mojangapi.world.biome.Biome biome = amidst.mojangapi.world.biome.Biome.getByName(b.getBiomeName());

    if (biome == null) {
      biome = new amidst.mojangapi.world.biome.Biome(b.getBiomeName(), idx,
          BiomeColor.from(random.nextInt(255), random.nextInt(255), random.nextInt(255)), BiomeType.OCEAN);
    }

  }
}
