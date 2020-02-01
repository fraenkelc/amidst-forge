package amidst.remote.shm;

import amidst.remote.BiomeEntry;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SimpleShmInterfaceTests {
    public static final int REPETITIONS = 1_000;
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    @Mock
    private TestAmidstInterface.BackingMethods methodMock;
    @InjectMocks
    private TestAmidstInterface amidstInterface;

    private AreaLock lock = LockSource.createSemaphoreLock();
    private ShmServer server;
    private ShmClient client;

    @Before
    public void setup() throws IOException {
        server = new ShmServer(amidstInterface);
        File sharedFile = temporaryFolder.newFile();
        server.startServer(new RandomAccessFile(sharedFile, "rw"), lock);
        client = new ShmClient(new RandomAccessFile(sharedFile, "rw"), lock);
    }

    @After
    public void tearDown() {
        server.shutdown();
    }

    @Test
    public void testCreateNewWorld() throws IOException, InterruptedException {
        // WHEN
        client.createNewWorld(12345L, "DEFAULT", "This is a Test", (r) -> {
            assertNotNull(r);
            return 0;
        });

        // THEN
        verify(methodMock).createNewWorld(eq(12345L), eq("DEFAULT"), eq("This is a Test"));
    }

    @Test
    public void testGetBiomeList() throws IOException, InterruptedException {
        // GIVEN
        Map<Integer, String> expected = new HashMap<>();
        expected.put(1, "Test 1");
        expected.put(2, "Test 2");
        expected.put(4, "Test 3");
        expected.put(8, "Test 4");
        given(methodMock.getBiomes()).willReturn(expected);

        // WHEN
        Map<Integer, String> actual = client.getBiomeList(biomeList -> {
            Map<Integer, String> result = new HashMap<>();
            BiomeEntry entry = new BiomeEntry();
            for (int i = 0; i < biomeList.biomesLength(); i++) {
                entry = biomeList.biomes(entry, i);
                result.put(entry.biomeId(), entry.biomeName());
            }
            return result;
        });

        // THEN
        assertEquals(expected, actual);
    }

    @Test
    public void testGetBiomeData() {
        // GIVEN
        int[] expected = new int[128 * 128];
        for (int i = 0; i < expected.length; i++) {
            expected[i] = i;
        }
        given(methodMock.getBiomeData(anyInt(), anyInt(), anyInt(), anyInt(), anyBoolean())).willReturn(expected);

        // WHEN
        int[] actual = client.getBiomeData(1, 2, 128, 128, true, biomeData -> {
            int[] data1 = new int[biomeData.dataLength()];
            for (int i = 0; i < data1.length; i++) {
                data1[i] = biomeData.data(i);
            }
            return data1;
        });

        // THEN
        assertArrayEquals(expected, actual);
        verify(methodMock).getBiomeData(1, 2, 128, 128, true);
    }

    @Test
    public void testConsecutiveCalls() {
        // GIVEN
        int[][] expected = new int[REPETITIONS][];
        for (int j = 0; j < REPETITIONS; j++) {
            expected[j] = new int[128 * 128];
            for (int i = 0; i < expected.length; i++) {
                expected[j][i] = i + j;
            }
            given(methodMock.getBiomeData(eq(j), anyInt(), anyInt(), anyInt(), anyBoolean())).willReturn(expected[j]);
        }

        // WHEN
        int[][] actual = new int[REPETITIONS][];
        for (int j = 0; j < REPETITIONS; j++) {
            actual[j] = client.getBiomeData(j, 2, 128, 128, true, biomeData -> {
                int[] data1 = new int[biomeData.dataLength()];
                for (int i = 0; i < data1.length; i++) {
                    data1[i] = biomeData.data(i);
                }
                return data1;
            });
        }

        // THEN
        for (int j = 0; j < REPETITIONS; j++) {
            assertArrayEquals(expected[j], actual[j]);
        }
        verify(methodMock, times(REPETITIONS)).getBiomeData(anyInt(), eq(2), eq(128), eq(128), eq(true));
    }
}
