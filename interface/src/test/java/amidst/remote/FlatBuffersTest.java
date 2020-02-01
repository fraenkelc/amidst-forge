package amidst.remote;

import amidst.remote.shm.FailingByteBufferFactory;
import com.google.flatbuffers.ByteBufferUtil;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Utf8;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import static org.junit.Assert.assertEquals;

public class FlatBuffersTest {
    private static int SIZE = 64;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testHeapBuffer() {
        // GIVEN
        ByteBuffer buffer = ByteBuffer.allocate(SIZE).order(ByteOrder.LITTLE_ENDIAN);
        // WHEN
        testBuffer(buffer);
    }

    @Test
    public void testDirectBuffer() {
        // GIVEN
        ByteBuffer buffer = ByteBuffer.allocateDirect(SIZE).order(ByteOrder.LITTLE_ENDIAN);
        // WHEN
        testBuffer(buffer);
    }

    @Test
    public void testMappedBuffer() throws IOException {
        // GIVEN
        ByteBuffer buffer = new RandomAccessFile(temporaryFolder.newFile(), "rw").getChannel()
                .map(FileChannel.MapMode.READ_WRITE, 0, SIZE)
                .order(ByteOrder.LITTLE_ENDIAN);
        // WHEN
        testBuffer(buffer);
    }

    @Test
    public void testOversizedBuffer() {
        // GIVEN
        ByteBuffer buffer = ByteBuffer.allocate(SIZE * 1024).order(ByteOrder.LITTLE_ENDIAN);
        // WHEN
        testBuffer(buffer);
    }

    private void testBuffer(ByteBuffer buffer) {
        FlatBufferBuilder builder = new FlatBufferBuilder(SIZE, FailingByteBufferFactory.INSTANCE, buffer, Utf8.getDefault());
        int offset = Header.createHeader(builder, 100, 0);
        builder.finishSizePrefixed(offset);

        // WHEN
        int messageSize = ByteBufferUtil.getSizePrefix(buffer);
        ByteBuffer newBuffer = ByteBufferUtil.removeSizePrefix(buffer).slice();
        newBuffer.limit(messageSize);
        Header header = Header.getRootAsHeader(newBuffer);

        // THEN
        assertEquals(100, header.messageNo());
    }
}
