package amidst.remote;

import com.google.flatbuffers.FlatBufferBuilder;
import org.xnio.ByteBufferPool;

import java.nio.ByteBuffer;

public class PooledByteBufferFactory extends FlatBufferBuilder.ByteBufferFactory {

    @Override
    public ByteBuffer newByteBuffer(int capacity) {
        return Utils.allocateBufferFromPool(capacity);
    }

    @Override
    public void releaseByteBuffer(ByteBuffer bb) {
        ByteBufferPool.free(bb);
    }

}
