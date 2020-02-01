package amidst.remote.shm;

import com.google.flatbuffers.FlatBufferBuilder;

import java.nio.ByteBuffer;

public class FailingByteBufferFactory extends FlatBufferBuilder.ByteBufferFactory {
    public static FlatBufferBuilder.ByteBufferFactory INSTANCE = new FailingByteBufferFactory();

    private FailingByteBufferFactory() {
    }

    @Override
    public ByteBuffer newByteBuffer(int capacity) {
        throw new RuntimeException("Tried to grow fixed size buffer to " + capacity + ". This should not happen.");
    }
}
