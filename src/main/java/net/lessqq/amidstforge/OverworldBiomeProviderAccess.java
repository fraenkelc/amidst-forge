package net.lessqq.amidstforge;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;

public class OverworldBiomeProviderAccess implements BiomeProviderAccess {
    private final MinecraftServer server;

    public OverworldBiomeProviderAccess(MinecraftServer server) {

        this.server = server;
    }

    @Override
    public BiomeProvider getBiomeProvider(long seed, WorldType worldType, String generatorOptions) {
        if (server != null) {
            ServerWorld world = server.getWorld(DimensionType.OVERWORLD);
            if (world != null) {
                return world.getChunkProvider().getChunkGenerator().getBiomeProvider();
            }
        }
        return null;
    }
}
