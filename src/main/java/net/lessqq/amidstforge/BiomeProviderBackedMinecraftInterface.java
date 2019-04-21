package net.lessqq.amidstforge;

import java.util.Random;

import amidst.mojangapi.minecraftinterface.MinecraftInterface;
import amidst.mojangapi.minecraftinterface.MinecraftInterfaceException;
import amidst.mojangapi.minecraftinterface.RecognisedVersion;
import amidst.mojangapi.world.biome.BiomeColor;
import amidst.mojangapi.world.biome.BiomeType;
import amidst.mojangapi.world.biome.UnknownBiomeIndexException;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public abstract class BiomeProviderBackedMinecraftInterface implements MinecraftInterface {

    private BiomeProvider biomeProvider;

    public BiomeProviderBackedMinecraftInterface(BiomeProvider biomeProvider) {
        this.setBiomeProvider(biomeProvider);
        updateBiomeList();
    }

    public BiomeProviderBackedMinecraftInterface() {
        updateBiomeList();
    }

    @Override
    public int[] getBiomeData(int x, int y, int width, int height, boolean useQuarterResolution)
            throws MinecraftInterfaceException {
        int[] biomeData = null;
        if ("net.minecraft.world.biome.BiomeProvider".equals(biomeProvider.getClass().getName()))
            biomeData = getBiomeDirect(x, y, width, height, useQuarterResolution);
        else
            biomeData = getBiomeFallback(x, y, width, height, useQuarterResolution);
        int[] results = new int[width * height];
        System.arraycopy(biomeData, 0, results, 0, results.length);
        checkForInvalidData(results);
        return results;
    }

    private void checkForInvalidData(int[] biomeData) {
        for (int i = 0; i < biomeData.length; i++)
            if (biomeData[i] < 0 || biomeData[i] > 255) {
                RuntimeException exception = new RuntimeException("BiomeProvider returned invalid Biome ID: '" + biomeData[i]
                    + "'. Your mod combination might not be supported.");
                exception.fillInStackTrace();
                AmidstForgeMod.LOGGER.error("Biome data check failed.", exception);
                throw exception;
            }
    }

    /**
     * get the biome data using direct field access.
     * 
     * This is faster and creates less garbage on the heap.
     */
    private int[] getBiomeDirect(int x, int y, int width, int height, boolean useQuarterResolution) {
        IntCache.resetIntCache();
        GenLayer layer;
        if (useQuarterResolution)
            layer = getBiomeProvider().genBiomes;
        else
            layer = getBiomeProvider().biomeIndexLayer;
        return layer.getInts(x, y, width, height).clone();
    }

    /**
     * Get the data using the official methods. Useful fallback for mods like
     * OpenTerrainGenerator
     */
    private int[] getBiomeFallback(int x, int y, int width, int height, boolean useQuarterResolution) {
        Biome[] biomes = new Biome[width * height];
        if (useQuarterResolution) {
            biomes = getBiomeProvider().getBiomesForGeneration(biomes, x, y, width, height);
        } else {
            biomes = getBiomeProvider().getBiomes(biomes, x, y, width, height);
        }
        int[] res = new int[width * height];
        for (int i = 0; i < res.length; i++) {
            res[i] = Biome.REGISTRY.getIDForObject(biomes[i]);
        }
        return res;
    }

    @Override
    public RecognisedVersion getRecognisedVersion() {
        return RecognisedVersion.UNKNOWN;
    }

    public BiomeProvider getBiomeProvider() {
        return biomeProvider;
    }

    public void setBiomeProvider(BiomeProvider biomeProvider) {
        this.biomeProvider = biomeProvider;
    }

    protected void updateBiomeList() {
        Random random = new Random(12354L);
        ForgeRegistries.BIOMES.getValuesCollection().forEach(b -> maybeAddBiome(random, b, Biome.REGISTRY.getIDForObject(b)));
    }

    private void maybeAddBiome(Random random, Biome b, int idx) {
        try {
            amidst.mojangapi.world.biome.Biome.getByIndex(idx);
        } catch (UnknownBiomeIndexException e) {
          // this constructor call has side effects.
          new amidst.mojangapi.world.biome.Biome(b.getBiomeName(), idx, BiomeColor.from(random.nextInt(255), random.nextInt(255), random.nextInt(255)), BiomeType.OCEAN);
        }
    }

}
