package net.lessqq.amidstforge;

import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.biome.provider.BiomeProviderType;
import net.minecraft.world.biome.provider.OverworldBiomeProviderSettings;
import net.minecraft.world.gen.OverworldGenSettings;
import net.minecraft.world.storage.WorldInfo;

public class IntegratedBiomeProviderAccess implements BiomeProviderAccess {
    @Override
    public BiomeProvider getBiomeProvider(long seed, WorldType worldType, String generatorOptions) {
        WorldSettings settings = new WorldSettings(seed, GameType.CREATIVE, true, false, worldType);
        OverworldBiomeProviderSettings biomeProviderSettings = BiomeProviderType.VANILLA_LAYERED.createSettings().setWorldInfo(new WorldInfo(settings, "AMIDSTWorld")).setGeneratorSettings(new OverworldGenSettings());
        return BiomeProviderType.VANILLA_LAYERED.create(biomeProviderSettings);
    }
}
