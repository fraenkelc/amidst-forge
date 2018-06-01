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

import java.util.Collection;

public class AmidstInterfaceImpl extends AmidstInterfaceGrpc.AmidstInterfaceImplBase {

    private BiomeDataAccessor biomeDataAccessor = new BiomeDataAccessor();
    private BiomeProviderAccess biomeProviderAccess = new IntegratedBiomeProviderAccess();

    @Override
    public void getBiomeData(BiomeDataRequest request, StreamObserver<BiomeDataReply> responseObserver) {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        try {
            int[] biomeData = biomeDataAccessor.getBiomeData(request.x(), request.y(), request.width(), request.height(), request.useQuarterResolution());
            int dataVector = BiomeDataReply.createDataVector(builder, biomeData);
            BiomeDataReply.startBiomeDataReply(builder);
            BiomeDataReply.addData(builder, dataVector);
            BiomeDataReply.endBiomeDataReply(builder);
            responseObserver.onNext(BiomeDataReply.getRootAsBiomeDataReply(builder.dataBuffer()));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void getBiomeList(GetBiomeListRequest request, StreamObserver<BiomeListReply> responseObserver) {
        try {
            FlatBufferBuilder builder = new FlatBufferBuilder();
            ForgeRegistry<Biome> biomeRegistry = (ForgeRegistry<Biome>) ForgeRegistries.BIOMES;
            Collection<Biome> biomeList = biomeRegistry.getValues();
            int[] biomeIds = new int[biomeList.size()];
            int i = 0;
            LanguageMap map = new LanguageMap();
            for (Biome b : biomeList) {
                int biomeName = builder.createString(map.translateKey(b.getTranslationKey()));
                biomeIds[i++] = BiomeEntry.createBiomeEntry(builder, biomeRegistry.getID(b), biomeName);
            }
            int biomesVector = BiomeListReply.createBiomesVector(builder, biomeIds);
            BiomeListReply.createBiomeListReply(builder, biomesVector);
            responseObserver.onNext(BiomeListReply.getRootAsBiomeListReply(builder.dataBuffer()));
            responseObserver.onCompleted();
        } catch (Exception e) {
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
            CreateNewWorldReply.endCreateNewWorldReply(builder);
            responseObserver.onNext(CreateNewWorldReply.getRootAsCreateNewWorldReply(builder.dataBuffer()));
            responseObserver.onCompleted();
        } catch (Exception e) {
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
