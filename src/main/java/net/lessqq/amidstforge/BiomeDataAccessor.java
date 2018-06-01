package net.lessqq.amidstforge;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
public class BiomeDataAccessor {
    private ForgeRegistry<Biome> biomeRegistry = (ForgeRegistry<Biome>) ForgeRegistries.BIOMES;
    private BiomeProvider biomeProvider;
    private volatile int[] dataArray = new int[256];
    private static final Logger logger = LogManager.getLogger(AmidstForgeMod.MOD_ID);

    public int[] getBiomeData(int x, int y, int width, int height, boolean useQuarterResolution) {
        int[] biomeData = getBiomes(x, y, width, height, useQuarterResolution);
        checkForInvalidData(biomeData);
        return biomeData;
    }

    private void checkForInvalidData(int[] biomeData)  {
        for (int biomeDatum : biomeData)
            if (biomeDatum < 0 || biomeDatum > 255) {
                RuntimeException exception = new RuntimeException("BiomeProvider returned invalid Biome ID: '" + biomeDatum
                    + "'. Your mod combination might not be supported.");
                exception.fillInStackTrace();
                logger.error("Biome data check failed.", exception);
                throw exception;
            }
    }

    /**
     * Get the data using the official methods. Useful fallback for mods like
     * OpenTerrainGenerator
     */
    private int[] getBiomes(int x, int y, int width, int height, boolean useQuarterResolution) {
        int[] data = ensureArrayCapacity(width * height);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int idx = i + j * width;
                Biome b;
        if (useQuarterResolution)
                    b = biomeProvider.func_222366_b(x + i, y + j);
        else
                    b = biomeProvider.getBiome(x + i, y + j);
                data[idx] = biomeRegistry.getID(b);
    }
        }
        return data;
        }

    public BiomeProvider getBiomeProvider() {
        return biomeProvider;
    }

    public void setBiomeProvider(BiomeProvider biomeProvider) {
        this.biomeProvider = biomeProvider;
    }
    private int[] ensureArrayCapacity(int length) {
        int cur = dataArray.length;
        if (length <= cur)
            return dataArray;

        while (cur < length)
            cur *= 2;

        dataArray = new int[cur];
        return dataArray;
    }
}
