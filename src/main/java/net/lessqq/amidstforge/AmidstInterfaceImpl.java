package net.lessqq.amidstforge;

import amidst.remote.*;
import com.google.flatbuffers.FlatBufferBuilder;
import net.minecraft.util.text.translation.LanguageMap;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;

public class AmidstInterfaceImpl implements AmidstInterface {
    private static final Logger logger = LogManager.getLogger(AmidstForgeMod.MOD_ID);

    private BiomeDataAccessor biomeDataAccessor = new BiomeDataAccessor();
    private BiomeProviderAccess biomeProviderAccess = new IntegratedBiomeProviderAccess();

    @Override
    public int getBiomeData(BiomeDataRequest request, FlatBufferBuilder builder) {
        int[] biomeData = biomeDataAccessor.getBiomeData(request.x(), request.y(), request.width(), request.height(), request.useQuarterResolution());
        int dataVector = BiomeDataReply.createDataVector(builder, biomeData);
        return BiomeDataReply.createBiomeDataReply(builder, dataVector);
    }

    @Override
    public int getBiomeList(GetBiomeListRequest request, FlatBufferBuilder builder) {
        ForgeRegistry<Biome> biomeRegistry = (ForgeRegistry<Biome>) ForgeRegistries.BIOMES;
        Collection<Biome> biomeList = biomeRegistry.getValues();
        int[] biomeNameOffsets = new int[biomeList.size()];
        int i = 0;
        LanguageMap map = new LanguageMap();
        for (Biome b : biomeList) {
            int biomeName = builder.createString(map.translateKey(b.getTranslationKey()));
            biomeNameOffsets[i++] = BiomeEntry.createBiomeEntry(builder, biomeRegistry.getID(b), biomeName);
        }
        BiomeListReply.startBiomesVector(builder, biomeNameOffsets.length);
        for (int biomeNameOffset : biomeNameOffsets) {
            builder.addOffset(biomeNameOffset);
        }
        int biomesVector = builder.endVector();
        return BiomeListReply.createBiomeListReply(builder, biomesVector);
    }

    @Override
    public int createNewWorld(CreateWorldRequest request, FlatBufferBuilder builder) {
        BiomeProvider biomeProvider = biomeProviderAccess.getBiomeProvider(request.seed(), mapWorldType(request.worldType()), request.generatorOptions());
        biomeDataAccessor.setBiomeProvider(biomeProvider);
        CreateNewWorldReply.startCreateNewWorldReply(builder);
        return CreateNewWorldReply.endCreateNewWorldReply(builder);
    }

    private WorldType mapWorldType(String worldType) {
        switch (worldType) {
            case "DEFAULT":
                return WorldType.DEFAULT;
            case "FLAT":
                return WorldType.FLAT;
            case "LARGE_BIOMES":
                return WorldType.LARGE_BIOMES;
            case "AMPLIFIED":
                return WorldType.AMPLIFIED;
            case "CUSTOMIZED":
                return WorldType.CUSTOMIZED;
            default:
                throw new RuntimeException("Unknown WorldType " + worldType);
        }
    }

    public void setBiomeProviderAccess(BiomeProviderAccess biomeProviderAccess) {
        this.biomeProviderAccess = biomeProviderAccess;
    }

}
