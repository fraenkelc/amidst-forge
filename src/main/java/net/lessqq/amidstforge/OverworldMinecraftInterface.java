package net.lessqq.amidstforge;

import amidst.mojangapi.minecraftinterface.MinecraftInterfaceException;
import amidst.mojangapi.world.WorldType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;

public class OverworldMinecraftInterface extends BiomeProviderBackedMinecraftInterface {

    private final MinecraftServer server;

    public OverworldMinecraftInterface(MinecraftServer server) {

        this.server = server;
    }

    @Override
    public void createWorld(long seed, WorldType worldType, String generatorOptions)
            throws MinecraftInterfaceException {
        if (server != null) {
            updateBiomeList();

            ServerWorld world = server.getWorld(DimensionType.OVERWORLD);
            setBiomeProvider(world.getChunkProvider().getChunkGenerator().getBiomeProvider());
        }
    }

}
