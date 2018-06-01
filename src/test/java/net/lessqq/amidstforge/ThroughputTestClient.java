package net.lessqq.amidstforge;

import amidst.remote.AmidstRemoteClient;
import amidst.remote.CreateNewWorldReply;
import org.HdrHistogram.Recorder;

public class ThroughputTestClient {
    public static final int SIZE = 128;
    private final AmidstRemoteClient amidstRemoteClient;
    private final Recorder recorder;

    public volatile int[] blackhole;

    public static void main(String[] args) {
        new ThroughputTestClient().run();

    }

    ThroughputTestClient() {
        amidstRemoteClient = new AmidstRemoteClient(AmidstForgeMod.AMIDST_REMOTE_PORT);
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
        return amidstRemoteClient.getBiomeData(x, y, width, height, useQuarterResolution, biomeData -> {
            int[] dt = new int[biomeData.dataLength()];
            for (int i = 0; i < dt.length; i++) {
                dt[i] = biomeData.data(i);
            }
            return dt;
        });
    }

    private void createWorld(long seed) {
        amidstRemoteClient.createNewWorld(seed, "DEFAULT", "", this::handleCreateWorldResponse);
    }

    private Void handleCreateWorldResponse(CreateNewWorldReply reply) {
        return null;
    }

}
