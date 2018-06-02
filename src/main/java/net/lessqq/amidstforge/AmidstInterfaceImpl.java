package net.lessqq.amidstforge;

import amidst.remote.*;
import com.google.flatbuffers.FlatBufferBuilder;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import io.grpc.stub.StreamObserver;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
            TIntArrayList biomes = new TIntArrayList();
            for (Biome b : ForgeRegistries.BIOMES) {
                int biomeName = builder.createString(b.getBiomeName());
                biomes.add(BiomeEntry.createBiomeEntry(builder, Biome.REGISTRY.getIDForObject(b), biomeName));
            }
            BiomeListReply.startBiomesVector(builder, biomes.size());
            for (TIntIterator iterator = biomes.iterator(); iterator.hasNext(); ) {
                builder.addOffset(iterator.next());
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
