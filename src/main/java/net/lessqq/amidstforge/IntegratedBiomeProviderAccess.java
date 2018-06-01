package net.lessqq.amidstforge;

import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.storage.WorldInfo;

public class IntegratedBiomeProviderAccess implements BiomeProviderAccess {
    @Override
    public BiomeProvider getBiomeProvider(long seed, WorldType worldType, String generatorOptions) {
        WorldSettings settings = new WorldSettings(seed, GameType.CREATIVE, true, false, worldType);
        settings.setGeneratorOptions(generatorOptions);
        return new BiomeProvider(new WorldInfo(settings, "AMIDSTWorld"));
    }
}
