package amidst.remote.shm;

import amidst.remote.BiomeEntry;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class MPShmInterfaceTests {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testMultiProcessCommunication() throws IOException, InterruptedException {
        // GIVEN
        File lockFile = temporaryFolder.newFile();
        ProcessBuilder serverBuilder = TestUtils.exec(Server.class, Collections.emptyList(), Arrays.asList(lockFile.getAbsolutePath()));
        serverBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);

        ProcessBuilder clientBuilder = TestUtils.exec(Client.class, Collections.emptyList(), Arrays.asList(lockFile.getAbsolutePath()));
        clientBuilder.inheritIO();

        // WHEN
        Process server = serverBuilder.start();
        // wait till process has started
        server.getErrorStream().read();
        Process client = clientBuilder.start();
        client.waitFor(20, TimeUnit.SECONDS);

        // THEN
        assertEquals(0, client.exitValue());

        // shutdown server
        server.getOutputStream().close();
        server.waitFor(2, TimeUnit.SECONDS);
        if (server.isAlive())
            server.destroyForcibly();
        assertEquals(0, server.exitValue());

    }


    public static class Server {
        public static void main(String[] args) throws IOException {
            File sharedFile = new File(args[0]);
            ShmServer shmServer = new ShmServer(new TestAmidstInterface(new TestMethodsImpl()));
            shmServer.startServer(sharedFile);
            // close the error stream to signal we're ready
            System.out.println("Server: Server is ready");
            System.err.close();
            while (System.in.read() != -1) {
                // wait for input to close
            }
            System.out.println("Server: Shutting down server");
            shmServer.shutdown();
        }

    }

    public static class Client {
        public static void main(String[] args) {
            File sharedFile = new File(args[0]);
            ShmClient client = new ShmClient(sharedFile);
            TestMethodsImpl source = new TestMethodsImpl();
            System.out.println("Client: Creating new world");
            // create a world
            client.createNewWorld(123154123L, "DEFAULT", "worldgen options", r -> "");

            // get the biome list
            System.out.println("Client: Getting Biome List");
            Map<Integer, String> expectedBiomes = source.getBiomes();
            Map<Integer, String> actualBiomes = client.getBiomeList(biomeList -> {
                Map<Integer, String> result = new HashMap<>();
                BiomeEntry entry = new BiomeEntry();
                for (int i = 0; i < biomeList.biomesLength(); i++) {
                    entry = biomeList.biomes(entry, i);
                    result.put(entry.biomeId(), entry.biomeName());
                }
                return result;
            });
            assertEquals(expectedBiomes, actualBiomes);

            // now request lots of biome data
            System.out.println("Client: Loading Biome Data");
            for (int j = 0; j < 50_000; j++) {
                int[] expected = source.getBiomeData(j, 2, 128, 128, true);
                int[] actual = client.getBiomeData(j, 2, 128, 128, true, biomeData -> {
                    int[] data1 = new int[biomeData.dataLength()];
                    for (int i = 0; i < data1.length; i++) {
                        data1[i] = biomeData.data(i);
                    }
                    return data1;
                });
                assertArrayEquals(expected, actual);
            }
        }
    }

    public static class TestMethodsImpl implements TestAmidstInterface.BackingMethods {

        @Override
        public int[] getBiomeData(int x, int y, int width, int height, boolean useQuarterResolution) {
            int[] expected = new int[width * height];
            for (int i = 0; i < expected.length; i++) {
                expected[i] = i + (width * height);
            }
            return expected;
        }

        @Override
        public Map<Integer, String> getBiomes() {
            Map<Integer, String> expected = new HashMap<>();
            for (int i = 0; i < 100; i++) {
                expected.put(i, "Biome " + i);
            }
            return expected;
        }

        @Override
        public void createNewWorld(long seed, String mapWorldType, String generatorOptions) {

        }
    }

}
