package amidst.remote;

import com.google.flatbuffers.ByteBufferUtil;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.FlatBufferBuilder;
import org.wildfly.common.Assert;
import org.xnio.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.function.Function;

import static amidst.remote.Utils.readFully;
import static amidst.remote.Utils.writeFully;

public class AmidstRemoteClient implements AutoCloseable {
    private StreamConnection streamConnection;
    private final FlatBufferBuilder builder = new FlatBufferBuilder(ByteBufferPool.MEDIUM_SIZE, new PooledByteBufferFactory());
    private final ByteBuffer size = ByteBuffer.allocateDirect(Constants.SIZE_PREFIX_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
    private final Response response = new Response();
    private final BiomeDataReply biomeDataReply = new BiomeDataReply();
    private final BiomeListReply biomeListReply = new BiomeListReply();
    private final CreateNewWorldReply createNewWorldReply = new CreateNewWorldReply();


    public AmidstRemoteClient(int port) {
        Xnio xnio = Xnio.getInstance();
        XnioWorker worker = xnio.createWorkerBuilder().build();
        IoFuture<StreamConnection> ioFuture = worker.openStreamConnection(new InetSocketAddress(InetAddress.getLoopbackAddress(), port), null, OptionMap.EMPTY);
        try {
            streamConnection = ioFuture.get();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to connect to remote host", e);
        }
    }

    @Override
    public void close() throws Exception {
        if (streamConnection != null) {
            streamConnection.close();
            streamConnection = null;
        }
    }


    public <T> T getBiomeData(int x, int y, int width, int height, boolean useQuarterResolution, Function<BiomeDataReply, T> consumer) {
        try {
            builder.clear();

            int biomeDataRequestOffset = BiomeDataRequest.createBiomeDataRequest(builder, x, y, width, height, useQuarterResolution);
            int requestOffset = Request.createRequest(builder, RequestTypes.BiomeDataRequest, biomeDataRequestOffset);

            builder.finishSizePrefixed(requestOffset);
            ByteBuffer buffer = builder.dataBuffer();
            writeFully(buffer, streamConnection.getSinkChannel());
            readFully(size, streamConnection.getSourceChannel());
            int sizePrefix = ByteBufferUtil.getSizePrefix(size);
            ByteBuffer byteBuffer = Utils.allocateBufferFromPool(sizePrefix);
            byteBuffer.limit(sizePrefix);
            readFully(byteBuffer, streamConnection.getSourceChannel());
            Response response = Response.getRootAsResponse(byteBuffer, this.response);

            Assert.assertTrue(response.dataType() == ResponseTypes.BiomeDataReply);
            T result = consumer.apply((BiomeDataReply) response.data(biomeDataReply));
            ByteBufferPool.free(byteBuffer);
            return result;
        } catch (IOException e) {
            UncheckedIOException ioException = new UncheckedIOException("IO error while calling remote interface", e);
            try {
                streamConnection.close();
            } catch (IOException e2) {
                ioException.addSuppressed(e2);
            }
            throw ioException;
        }
    }


    public <T> T getBiomeList(Function<BiomeListReply, T> consumer) {
        try {
            builder.clear();
            GetBiomeListRequest.startGetBiomeListRequest(builder);
            int dataRequestOffset = GetBiomeListRequest.endGetBiomeListRequest(builder);
            int requestOffset = Request.createRequest(builder, RequestTypes.GetBiomeListRequest, dataRequestOffset);

            builder.finishSizePrefixed(requestOffset);
            ByteBuffer buffer = builder.dataBuffer();
            writeFully(buffer, streamConnection.getSinkChannel());
            readFully(size, streamConnection.getSourceChannel());
            int sizePrefix = ByteBufferUtil.getSizePrefix(size);
            ByteBuffer byteBuffer = Utils.allocateBufferFromPool(sizePrefix);
            byteBuffer.limit(sizePrefix);
            readFully(byteBuffer, streamConnection.getSourceChannel());
            Response response = Response.getRootAsResponse(byteBuffer, this.response);

            Assert.assertTrue(response.dataType() == ResponseTypes.BiomeListReply);
            T result = consumer.apply((BiomeListReply) response.data(biomeListReply));
            ByteBufferPool.free(byteBuffer);
            return result;
        } catch (IOException e) {
            UncheckedIOException ioException = new UncheckedIOException("IO error while calling remote interface", e);
            try {
                streamConnection.close();
            } catch (IOException e2) {
                ioException.addSuppressed(e2);
            }
            throw ioException;
        }
    }


    public <T> T createNewWorld(long seed, String worldType, String generatorOptions, Function<CreateNewWorldReply, T> consumer) {
        try {
            builder.clear();
            int worldTypeOffset = builder.createString(worldType);
            int generatorOptionsOffset = builder.createString(generatorOptions);
            int dataRequestOffset = CreateWorldRequest.createCreateWorldRequest(builder, seed, worldTypeOffset, generatorOptionsOffset);
            int requestOffset = Request.createRequest(builder, RequestTypes.CreateWorldRequest, dataRequestOffset);

            builder.finishSizePrefixed(requestOffset);
            ByteBuffer buffer = builder.dataBuffer();
            writeFully(buffer, streamConnection.getSinkChannel());
            readFully(size, streamConnection.getSourceChannel());
            int sizePrefix = ByteBufferUtil.getSizePrefix(size);
            ByteBuffer byteBuffer = Utils.allocateBufferFromPool(sizePrefix);
            byteBuffer.limit(sizePrefix);
            readFully(byteBuffer, streamConnection.getSourceChannel());
            Response response = Response.getRootAsResponse(byteBuffer, this.response);

            Assert.assertTrue(response.dataType() == ResponseTypes.BiomeListReply);
            T result = consumer.apply((CreateNewWorldReply) response.data(createNewWorldReply));
            ByteBufferPool.free(byteBuffer);
            return result;
        } catch (IOException e) {
            UncheckedIOException ioException = new UncheckedIOException("IO error while calling remote interface", e);
            try {
                streamConnection.close();
            } catch (IOException e2) {
                ioException.addSuppressed(e2);
            }
            throw ioException;
        }
    }

}
