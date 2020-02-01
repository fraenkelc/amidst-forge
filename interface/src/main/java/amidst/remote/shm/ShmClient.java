package amidst.remote.shm;

import amidst.remote.*;
import com.google.flatbuffers.ByteBufferUtil;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Utf8;
import org.wildfly.common.Assert;
import org.xnio.ByteBufferPool;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;

public class ShmClient {
    private final RandomAccessFile sharedMemoryFile;
    private final AreaLock lock;

    private boolean isOpen = true;
    private Header requestHeader;
    private Header responseHeader;
    private ByteBuffer byteBuffer;

    private final Response response = new Response();
    private final BiomeDataReply biomeDataReply = new BiomeDataReply();
    private final BiomeListReply biomeListReply = new BiomeListReply();
    private final CreateNewWorldReply createNewWorldReply = new CreateNewWorldReply();


    public ShmClient(File sharedFile) {
        RandomAccessFile randomAccessFile;
        try {
            randomAccessFile = new RandomAccessFile(sharedFile, "rw");
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException("Failed to prepare shm file", e);
        }
        this.sharedMemoryFile = randomAccessFile;
        this.lock = LockSource.createIPCLock(randomAccessFile.getChannel());
        initializeClient();
    }

    protected ShmClient(RandomAccessFile sharedMemoryFile, AreaLock lock) {
        this.sharedMemoryFile = sharedMemoryFile;
        this.lock = lock;
        initializeClient();
    }

    private void initializeClient() {
        try {
            byteBuffer = sharedMemoryFile.getChannel()
                    .map(FileChannel.MapMode.READ_WRITE, 0, Constants.TOTAL_BUFFER_SIZE)
                    .order(ByteOrder.LITTLE_ENDIAN);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to map shared memory area", e);
        }
        acquireLock();
        this.requestHeader = readHeader(Constants.REQUEST_AREA_START, Constants.REQUEST_AREA_HEADER_SIZE, byteBuffer);
        this.responseHeader = readHeader(Constants.RESPONSE_AREA_START, Constants.RESPONSE_AREA_HEADER_SIZE, byteBuffer);
    }

    private Header readHeader(int areaStart, int headerSize, ByteBuffer byteBuffer) {
        byteBuffer.position(areaStart);
        ByteBuffer headerBuffer = byteBuffer.slice().order(ByteOrder.LITTLE_ENDIAN);
        headerBuffer.position(headerBuffer.getInt(0));
        return Header.getRootAsHeader(headerBuffer);
    }

    private void acquireLock() {
        try {
            lock.lock();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            isOpen = false;
            throw new RuntimeException("Interrupted while waiting for Lock");
        }
    }

    private void checkOpen() {
        if (!isOpen) {
            throw new RuntimeException("Server connection is not open.");
        }
    }

    private FlatBufferBuilder getRequestBuilder() {
        byteBuffer.position(Constants.REQUEST_AREA_DATA_START);
        ByteBuffer requestBuffer = byteBuffer.slice();
        requestBuffer.limit(Constants.REQUEST_AREA_DATA_SIZE);

        return new FlatBufferBuilder(Constants.REQUEST_AREA_DATA_SIZE, FailingByteBufferFactory.INSTANCE, requestBuffer, Utf8.getDefault());
    }

    private void waitForResponse() {
        long msgNo = responseHeader.messageNo();
        int tries = 100;
        while (tries-- > 0 && msgNo == responseHeader.messageNo()) {
            lock.unlock();
            try {
                lock.lock();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                isOpen = false;
                throw new RuntimeException("Interrupted while waiting for Lock");
            }
        }
        if (msgNo == responseHeader.messageNo()) {
            // Server closed, shutdown client
            isOpen = false;
            throw new RuntimeException("Server is not available");
        }
    }

    private Response getResponseFromResponseArea() {
        byteBuffer.position(Constants.RESPONSE_AREA_DATA_START);
        ByteBuffer responseBuffer = byteBuffer.slice().order(ByteOrder.LITTLE_ENDIAN);
        responseBuffer.limit(Constants.RESPONSE_AREA_DATA_SIZE);

        responseBuffer.position(responseHeader.dataPos());
        return Response.getRootAsResponse(responseBuffer, response);

    }

    public <T> T getBiomeData(int x, int y, int width, int height, boolean useQuarterResolution, Function<BiomeDataReply, T> consumer) {
        checkOpen();

        FlatBufferBuilder builder = getRequestBuilder();

        int biomeDataRequestOffset = BiomeDataRequest.createBiomeDataRequest(builder, x, y, width, height, useQuarterResolution);
        int requestOffset = Request.createRequest(builder, RequestTypes.BiomeDataRequest, biomeDataRequestOffset);
        builder.finish(requestOffset);
        requestHeader.mutateDataPos(builder.dataBuffer().position());
        requestHeader.mutateMessageNo(requestHeader.messageNo() + 1);

        waitForResponse();
        Response response = getResponseFromResponseArea();

        Assert.assertTrue(response.dataType() == ResponseTypes.BiomeDataReply);
        return consumer.apply((BiomeDataReply) response.data(biomeDataReply));
    }

    public <T> T getBiomeList(Function<BiomeListReply, T> consumer) {
        FlatBufferBuilder builder = getRequestBuilder();

        GetBiomeListRequest.startGetBiomeListRequest(builder);
        int dataRequestOffset = GetBiomeListRequest.endGetBiomeListRequest(builder);
        int requestOffset = Request.createRequest(builder, RequestTypes.GetBiomeListRequest, dataRequestOffset);
        builder.finish(requestOffset);
        requestHeader.mutateDataPos(builder.dataBuffer().position());
        requestHeader.mutateMessageNo(requestHeader.messageNo() + 1);

        waitForResponse();
        Response response = getResponseFromResponseArea();

        Assert.assertTrue(response.dataType() == ResponseTypes.BiomeListReply);
        return consumer.apply((BiomeListReply) response.data(biomeListReply));
    }


    public <T> T createNewWorld(long seed, String worldType, String generatorOptions, Function<CreateNewWorldReply, T> consumer) {
        FlatBufferBuilder builder = getRequestBuilder();
        int worldTypeOffset = builder.createString(worldType);
        int generatorOptionsOffset = builder.createString(generatorOptions);
        int dataRequestOffset = CreateWorldRequest.createCreateWorldRequest(builder, seed, worldTypeOffset, generatorOptionsOffset);
        int requestOffset = Request.createRequest(builder, RequestTypes.CreateWorldRequest, dataRequestOffset);
        builder.finish(requestOffset);
        requestHeader.mutateDataPos(builder.dataBuffer().position());
        requestHeader.mutateMessageNo(requestHeader.messageNo() + 1);

        waitForResponse();
        Response response = getResponseFromResponseArea();

        Assert.assertTrue(response.dataType() == ResponseTypes.CreateNewWorldReply);
        return consumer.apply((CreateNewWorldReply) response.data(createNewWorldReply));
    }

}
