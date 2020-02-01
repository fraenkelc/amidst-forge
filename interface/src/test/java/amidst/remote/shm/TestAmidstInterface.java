package amidst.remote.shm;

import amidst.remote.*;
import com.google.flatbuffers.FlatBufferBuilder;

import java.util.Map;

public class TestAmidstInterface implements AmidstInterface {

    private final BackingMethods backingMethods;

    public TestAmidstInterface(BackingMethods backingMethods) {
        this.backingMethods = backingMethods;
    }

    @Override
    public int getBiomeData(BiomeDataRequest request, FlatBufferBuilder builder) {
        int[] biomeData = backingMethods.getBiomeData(request.x(), request.y(), request.width(), request.height(), request.useQuarterResolution());
        int dataVector = BiomeDataReply.createDataVector(builder, biomeData);
        return BiomeDataReply.createBiomeDataReply(builder, dataVector);
    }

    @Override
    public int getBiomeList(GetBiomeListRequest request, FlatBufferBuilder builder) {
        Map<Integer, String> biomes = backingMethods.getBiomes();
        int[] biomeNameOffsets = new int[biomes.size()];
        int i = 0;
        for (Map.Entry<Integer, String> b : biomes.entrySet()) {
            int biomeName = builder.createString(b.getValue());
            biomeNameOffsets[i++] = BiomeEntry.createBiomeEntry(builder, b.getKey(), biomeName);
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
        backingMethods.createNewWorld(request.seed(), request.worldType(), request.generatorOptions());
        CreateNewWorldReply.startCreateNewWorldReply(builder);
        return CreateNewWorldReply.endCreateNewWorldReply(builder);
    }

    public interface BackingMethods {
        int[] getBiomeData(int x, int y, int width, int height, boolean useQuarterResolution);

        Map<Integer, String> getBiomes();

        void createNewWorld(long seed, String mapWorldType, String generatorOptions);
    }
}
