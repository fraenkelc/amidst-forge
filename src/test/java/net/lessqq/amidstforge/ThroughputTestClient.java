package net.lessqq.amidstforge;

import amidst.remote.AmidstInterfaceGrpc;
import amidst.remote.BiomeDataReply;
import amidst.remote.BiomeDataRequest;
import amidst.remote.CreateWorldRequest;
import com.google.flatbuffers.FlatBufferBuilder;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.HdrHistogram.Recorder;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

public class ThroughputTestClient {
    public static final int SIZE = 128;
    private final AmidstInterfaceGrpc.AmidstInterfaceBlockingStub remoteInterface;
    private final Recorder recorder;

    public volatile int[] blackhole;

    public static void main(String[] args) {
        new ThroughputTestClient().run();

    }

    ThroughputTestClient() {
        Configurator.setLevel("io.grpc.netty.shaded.io.grpc.netty.NettyServerHandler", Level.WARN);
        Configurator.setLevel("io.grpc.netty.shaded.io.grpc.netty.NettyClientHandler", Level.WARN);
        ManagedChannel channel = ManagedChannelBuilder.forTarget("127.0.0.1:" + AmidstForgeMod.AMIDST_REMOTE_PORT).usePlaintext().build();
        remoteInterface = AmidstInterfaceGrpc.newBlockingStub(channel);
        recorder = new Recorder(3600000000000L, 3);
    }

    private void run() {
        createWorld(12345L);
        int curX = 0;
        for (int i = 0; i < 1_000; i++) {
            long start = System.nanoTime();
            blackhole = getBiomeData(curX += SIZE, 0, SIZE, SIZE, true);
            long diff = System.nanoTime() - start;
            recorder.recordValue(diff);
            if (i % 100 == 0) {
                System.out.println(i);
            }
        }
        recorder.getIntervalHistogram()
                .outputPercentileDistribution(System.out, 5, 1000.0, false);


    }


    private int[] getBiomeData(int x, int y, int width, int height, boolean useQuarterResolution) {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        int biomeDataRequest = BiomeDataRequest.createBiomeDataRequest(builder, x, y, width, height, useQuarterResolution);
        builder.finish(biomeDataRequest);
        BiomeDataReply biomeData = remoteInterface.getBiomeData(BiomeDataRequest.getRootAsBiomeDataRequest(builder.dataBuffer()));
        int[] data = new int[biomeData.dataLength()];
        for (int i = 0; i < data.length; i++) {
            data[i] = biomeData.data(i);
        }
        return data;
    }

    private void createWorld(long seed) {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        int worldTypeOffset = builder.createString("DEFAULT");
        int generatorOptionsOffset = builder.createString("");
        int createWorldRequest = CreateWorldRequest.createCreateWorldRequest(builder, seed, worldTypeOffset, generatorOptionsOffset);
        builder.finish(createWorldRequest);
        remoteInterface.createNewWorld(CreateWorldRequest.getRootAsCreateWorldRequest(builder.dataBuffer()));

    }

}
