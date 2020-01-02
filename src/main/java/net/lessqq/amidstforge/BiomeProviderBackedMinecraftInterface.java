package net.lessqq.amidstforge;

import amidst.mojangapi.minecraftinterface.MinecraftInterface;
import amidst.mojangapi.minecraftinterface.MinecraftInterfaceException;
import amidst.mojangapi.minecraftinterface.RecognisedVersion;
import amidst.mojangapi.world.biome.BiomeColor;
import amidst.mojangapi.world.biome.BiomeType;
import amidst.mojangapi.world.biome.UnknownBiomeIndexException;
import net.minecraft.util.text.translation.LanguageMap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

import java.util.Random;

public abstract class BiomeProviderBackedMinecraftInterface implements MinecraftInterface {

    private ForgeRegistry<Biome> biomeRegistry = (ForgeRegistry<Biome>) ForgeRegistries.BIOMES;
    private BiomeProvider biomeProvider;
    private volatile int[] dataArray = new int[256];

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
        int[] biomeData = getBiomes(x, y, width, height, useQuarterResolution);
        checkForInvalidData(biomeData);
        return biomeData;
    }

    private void checkForInvalidData(int[] biomeData) throws MinecraftInterfaceException {
        for (int biomeDatum : biomeData)
            if (biomeDatum < 0 || biomeDatum > 255) {
                MinecraftInterfaceException exception = new MinecraftInterfaceException("BiomeProvider returned invalid Biome ID: '" + biomeDatum
                        + "'. Your mod combination might not be supported.");
                exception.fillInStackTrace();
                AmidstForgeMod.LOGGER.error("Biome data check failed.", exception);
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
        ForgeRegistries.BIOMES.getValues().forEach(b -> maybeAddBiome(random, b, ((ForgeRegistry<Biome>) ForgeRegistries.BIOMES).getID(b)));
    }

    private void maybeAddBiome(Random random, Biome b, int idx) {
        LanguageMap map = new LanguageMap();
        try {
            amidst.mojangapi.world.biome.Biome.getByIndex(idx);
        } catch (UnknownBiomeIndexException e) {
            // this constructor call has side effects.
            new amidst.mojangapi.world.biome.Biome(map.translateKey(b.getTranslationKey()), idx, BiomeColor.from(random.nextInt(255), random.nextInt(255), random.nextInt(255)), BiomeType.OCEAN);
        }
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
