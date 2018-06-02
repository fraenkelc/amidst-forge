package net.lessqq.amidstforge;

import amidst.remote.*;
import com.google.flatbuffers.FlatBufferBuilder;
import io.grpc.stub.StreamObserver;
import net.minecraft.util.text.translation.LanguageMap;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;

public class AmidstInterfaceImpl extends AmidstInterfaceGrpc.AmidstInterfaceImplBase {
    private static final Logger logger = LogManager.getLogger(AmidstForgeMod.MOD_ID);

    private BiomeDataAccessor biomeDataAccessor = new BiomeDataAccessor();
    private BiomeProviderAccess biomeProviderAccess = new IntegratedBiomeProviderAccess();

    @Override
    public void getBiomeData(BiomeDataRequest request, StreamObserver<BiomeDataReply> responseObserver) {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        try {
            int[] biomeData = biomeDataAccessor.getBiomeData(request.x(), request.y(), request.width(), request.height(), request.useQuarterResolution());
            int dataVector = BiomeDataReply.createDataVector(builder, biomeData);
            int reply = BiomeDataReply.createBiomeDataReply(builder, dataVector);
            builder.finish(reply);
            responseObserver.onNext(BiomeDataReply.getRootAsBiomeDataReply(builder.dataBuffer()));
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed to retrieve biome data.", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getBiomeList(GetBiomeListRequest request, StreamObserver<BiomeListReply> responseObserver) {
        try {
            FlatBufferBuilder builder = new FlatBufferBuilder();
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
            for (int j = 0; j < biomeNameOffsets.length; j++) {
                builder.addOffset(biomeNameOffsets[j]);
            }
            int biomesVector = builder.endVector();
            int biomeListReply = BiomeListReply.createBiomeListReply(builder, biomesVector);
            builder.finish(biomeListReply);
            responseObserver.onNext(BiomeListReply.getRootAsBiomeListReply(builder.dataBuffer()));
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed to transfer biome list.", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void createNewWorld(CreateWorldRequest request, StreamObserver<CreateNewWorldReply> responseObserver) {
        try {
            BiomeProvider biomeProvider = biomeProviderAccess.getBiomeProvider(request.seed(), mapWorldType(request.worldType()), request.generatorOptions());
            biomeDataAccessor.setBiomeProvider(biomeProvider);
            FlatBufferBuilder builder = new FlatBufferBuilder(10);
            CreateNewWorldReply.startCreateNewWorldReply(builder);
            int reply = CreateNewWorldReply.endCreateNewWorldReply(builder);
            builder.finish(reply);
            responseObserver.onNext(CreateNewWorldReply.getRootAsCreateNewWorldReply(builder.dataBuffer()));
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed to create world.", e);
            responseObserver.onError(e);
        }
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
