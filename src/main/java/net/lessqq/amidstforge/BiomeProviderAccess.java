package net.lessqq.amidstforge;

import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeProvider;

public interface BiomeProviderAccess {
    BiomeProvider getBiomeProvider(long seed, WorldType worldType, String generatorOptions);
}
