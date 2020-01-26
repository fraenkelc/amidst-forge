package amidst.remote;

import com.google.flatbuffers.ByteBufferUtil;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.FlatBufferBuilder;
import org.xnio.*;
import org.xnio.channels.AcceptingChannel;
import org.xnio.conduits.ConduitStreamSourceChannel;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AmidstRemoteServer implements AutoCloseable {
    private final AmidstInterface amidstInterface;
    private AcceptingChannel<StreamConnection> server;

    public AmidstRemoteServer(int port, AmidstInterface amidstInterface, URLClassLoader classLoader) {
        this.amidstInterface = amidstInterface;
        try {
            Xnio xnio = Xnio.getInstance(classLoader);
            server = xnio.createWorkerBuilder().build().createStreamConnectionServer(new InetSocketAddress(InetAddress.getLoopbackAddress(), port), this::handleConnection, OptionMap.EMPTY);
            server.resumeAccepts();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to open Server Port " + port, e);
        }
    }

    private void handleConnection(AcceptingChannel<StreamConnection> channel) {
        try {
            StreamConnection accepted;
            while ((accepted = channel.accept()) != null) {
                accepted.getSourceChannel().getReadSetter().set(new ConduitStreamSourceChannelChannelListener(amidstInterface, accepted));
                accepted.getSourceChannel().resumeReads();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Error while handling new connection", e);
        }
    }


    @Override
    public void close() throws Exception {
        if (server != null) {
            server.close();
            server = null;
        }
    }

    private static class ConduitStreamSourceChannelChannelListener implements ChannelListener<ConduitStreamSourceChannel> {
        private final AmidstInterface amidstInterface;

        private final Request request;
        private final BiomeDataRequest biomeDataRequest;
        private final CreateWorldRequest createWorldRequest;
        private final GetBiomeListRequest getBiomeListRequest;

        private final ByteBuffer size;
        private final ByteBuffer buffer;
        private final FlatBufferBuilder builder;
        private final StreamConnection connection;

        public ConduitStreamSourceChannelChannelListener(AmidstInterface amidstInterface, StreamConnection connection) {
            this.amidstInterface = amidstInterface;
            this.connection = connection;
            request = new Request();
            biomeDataRequest = new BiomeDataRequest();
            createWorldRequest = new CreateWorldRequest();
            getBiomeListRequest = new GetBiomeListRequest();
            size = ByteBuffer.allocateDirect(Constants.SIZE_PREFIX_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
            size.limit(Constants.SIZE_PREFIX_LENGTH);
            buffer = ByteBuffer.allocateDirect(20480).order(ByteOrder.LITTLE_ENDIAN);
            builder = new FlatBufferBuilder(ByteBufferPool.MEDIUM_SIZE, new PooledByteBufferFactory());
        }

        @Override
        public void handleEvent(ConduitStreamSourceChannel conduitStreamSourceChannel) {
            try {
                if (!conduitStreamSourceChannel.isOpen())
                    return;
                size.rewind();
                Utils.readFully(size, conduitStreamSourceChannel);
                buffer.rewind();
                int messageSize = ByteBufferUtil.getSizePrefix(size);
                buffer.limit(messageSize);
                Utils.readFully(buffer, conduitStreamSourceChannel);
                Request.getRootAsRequest(buffer, request);
                builder.clear();
                byte dataType;
                int dataOffset;
                switch (request.dataType()) {
                    case RequestTypes.BiomeDataRequest:
                        dataOffset = amidstInterface.getBiomeData((BiomeDataRequest) request.data(biomeDataRequest), builder);
                        dataType = ResponseTypes.BiomeDataReply;
                        break;
                    case RequestTypes.CreateWorldRequest:
                        dataOffset = amidstInterface.createNewWorld((CreateWorldRequest) request.data(createWorldRequest), builder);
                        dataType = ResponseTypes.CreateNewWorldReply;
                        break;
                    case RequestTypes.GetBiomeListRequest:
                        dataOffset = amidstInterface.getBiomeList((GetBiomeListRequest) request.data(getBiomeListRequest), builder);
                        dataType = ResponseTypes.BiomeListReply;
                        break;
                    default:
                        throw new RuntimeException("Unknown Request type " + request.dataType());
                }
                int responseOffset = Response.createResponse(builder, dataType, dataOffset);
                builder.finishSizePrefixed(responseOffset);
                Utils.writeFully(builder.dataBuffer(), connection.getSinkChannel());
            } catch (IOException e) {
                UncheckedIOException ioException = new UncheckedIOException("Failed to process request", e);
                try {
                    connection.close();
                } catch (IOException ex) {
                    ioException.addSuppressed(ex);
                }
                throw ioException;
            }
        }
    }
}

