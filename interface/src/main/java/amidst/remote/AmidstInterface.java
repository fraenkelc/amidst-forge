package amidst.remote;

import com.google.flatbuffers.FlatBufferBuilder;

public interface AmidstInterface {

    int getBiomeData(amidst.remote.BiomeDataRequest request, FlatBufferBuilder builder);

    int getBiomeList(amidst.remote.GetBiomeListRequest request, FlatBufferBuilder builder);

    int createNewWorld(amidst.remote.CreateWorldRequest request, FlatBufferBuilder builder);
}