package net.lessqq.amidstforge;

import amidst.mojangapi.minecraftinterface.MinecraftInterfaceException;
import amidst.mojangapi.world.WorldType;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.storage.WorldInfo;

public class IntegratedMinecraftInterface extends BiomeProviderBackedMinecraftInterface {

    @Override
    public void createWorld(long seed, WorldType worldType, String generatorOptions)
            throws MinecraftInterfaceException {
        updateBiomeList();
        WorldSettings settings = new WorldSettings(seed, GameType.CREATIVE, true, false, getWorldType(worldType));
        settings.setGeneratorOptions(generatorOptions);
        setBiomeProvider(new BiomeProvider(new WorldInfo(settings, "AMIDSTWorld")));
    }

    private net.minecraft.world.WorldType getWorldType(WorldType worldType) throws MinecraftInterfaceException {
        switch (worldType) {
        case AMPLIFIED:
            return net.minecraft.world.WorldType.AMPLIFIED;
        case CUSTOMIZED:
            return net.minecraft.world.WorldType.CUSTOMIZED;
        case DEFAULT:
            return net.minecraft.world.WorldType.DEFAULT;
        case FLAT:
            return net.minecraft.world.WorldType.FLAT;
        case LARGE_BIOMES:
            return net.minecraft.world.WorldType.LARGE_BIOMES;
        }
        throw new MinecraftInterfaceException("Unknown World type '" + worldType + "'");
    }

}
