package net.lessqq.amidstforge;

import amidst.mojangapi.minecraftinterface.MinecraftInterfaceException;
import amidst.mojangapi.world.WorldType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class OverworldMinecraftInterface extends BiomeProviderBackedMinecraftInterface {

    @Override
    public void createWorld(long seed, WorldType worldType, String generatorOptions)
            throws MinecraftInterfaceException {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server != null) {
            updateBiomeList();
            WorldServer world = server.getWorld(0);
            if (world != null) {
                setBiomeProvider(world.getBiomeProvider());
            }
        }
    }

}
