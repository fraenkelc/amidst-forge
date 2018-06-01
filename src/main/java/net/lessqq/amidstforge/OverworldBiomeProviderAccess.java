package net.lessqq.amidstforge;

import net.lessqq.amidstforge.BiomeProviderAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class OverworldBiomeProviderAccess implements BiomeProviderAccess {
    @Override
    public BiomeProvider getBiomeProvider(long seed, WorldType worldType, String generatorOptions) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server != null) {
            WorldServer world = server.getWorld(0);
            if (world != null) {
                return world.getBiomeProvider();
            }
        }
        return null;
    }
}
