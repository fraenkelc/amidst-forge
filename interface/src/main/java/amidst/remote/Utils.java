package amidst.remote;

import org.xnio.ByteBufferPool;
import org.xnio.channels.Channels;
import org.xnio.conduits.ConduitStreamSinkChannel;
import org.xnio.conduits.ConduitStreamSourceChannel;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Utils {
    private Utils() {
        // singleton helper
    }

    public static void readFully(ByteBuffer buffer, ConduitStreamSourceChannel sourceChannel) throws IOException {
        while (buffer.hasRemaining())
            if (sourceChannel.read(buffer) == -1) {
                throw new EOFException();
            }
        buffer.flip();
    }

    public static void writeFully(ByteBuffer buffer, ConduitStreamSinkChannel sinkChannel) throws IOException {
        Channels.writeBlocking(sinkChannel, buffer);
        Channels.flushBlocking(sinkChannel);
    }

    public static ByteBuffer allocateBufferFromPool(int capacity) {
        ByteBufferPool.Set set = ByteBufferPool.Set.DIRECT;
        ByteBufferPool bufferPool;
        if (capacity <= ByteBufferPool.SMALL_SIZE) {
            bufferPool = set.getSmall();
        } else if (capacity <= ByteBufferPool.MEDIUM_SIZE) {
            bufferPool = set.getNormal();
        } else if (capacity <= ByteBufferPool.LARGE_SIZE) {
            bufferPool = set.getLarge();
        } else {
            throw new RuntimeException("Unexpected large allocation: " + capacity);
        }
        return bufferPool.allocate().order(ByteOrder.LITTLE_ENDIAN);
    }
}
