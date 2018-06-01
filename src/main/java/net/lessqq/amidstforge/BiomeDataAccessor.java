package net.lessqq.amidstforge;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BiomeDataAccessor {
    private BiomeProvider biomeProvider;
    private static final Logger logger = LogManager.getLogger(AmidstForgeMod.MOD_ID);

    public int[] getBiomeData(int x, int y, int width, int height, boolean useQuarterResolution) {
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
                logger.error("Biome data check failed.", exception);
                throw exception;
            }
    }

    /**
     * get the biome data using direct field access.
     * <p>
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

    public BiomeProvider getBiomeProvider() {
        return biomeProvider;
    }

    public void setBiomeProvider(BiomeProvider biomeProvider) {
        this.biomeProvider = biomeProvider;
    }
}
