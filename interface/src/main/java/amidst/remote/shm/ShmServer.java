package amidst.remote.shm;

import amidst.remote.*;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Utf8;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.WeakHashMap;
import java.util.concurrent.locks.LockSupport;

public class ShmServer {
    private static final ThreadGroup REMOTE_GROUP = new ThreadGroup("AmidstRemoteWorkerThreads");
    private final AmidstInterface amidstInterface;

    private WeakHashMap<ShmServerWorker, Object> workers = new WeakHashMap<>();
    private int threadNo = 0;

    public ShmServer(AmidstInterface amidstInterface) {
        this.amidstInterface = amidstInterface;
    }

    public void startServer(File sharedFile) {
        RandomAccessFile randomAccessFile;
        try {
            randomAccessFile = new RandomAccessFile(sharedFile, "rw");
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException("Failed to prepare shm file", e);
        }
        startServer(randomAccessFile, LockSource.createIPCLock(randomAccessFile.getChannel()));
    }

    protected void startServer(RandomAccessFile sharedMemoryFile, AreaLock lock) {
        ShmServerWorker worker = new ShmServerWorker(amidstInterface, sharedMemoryFile, lock);
        workers.put(worker, new Object());
        Thread serverThread = new Thread(REMOTE_GROUP, worker, "AmidstRemoteWorkerThread-" + threadNo++);
        serverThread.setDaemon(true);
        serverThread.start();
        while (!worker.isRunning) {
            Thread.yield();
        }
    }


    public void shutdown() {
        for (ShmServerWorker worker : workers.keySet()) {
            worker.shutdown();
        }
        REMOTE_GROUP.interrupt();
    }

    private static class ShmServerWorker implements Runnable {

        private final AmidstInterface amidstInterface;
        private final RandomAccessFile sharedMemoryFile;
        private final AreaLock lock;
        private final ByteBuffer byteBuffer;
        private volatile boolean doRun = true;
        private volatile boolean isRunning = false;

        private Header requestHeader;
        private Header responseHeader;
        private long previousRequestNo = 0L;

        private final Request request = new Request();
        private final BiomeDataRequest biomeDataRequest = new BiomeDataRequest();
        private final CreateWorldRequest createWorldRequest = new CreateWorldRequest();
        private final GetBiomeListRequest getBiomeListRequest = new GetBiomeListRequest();

        public ShmServerWorker(AmidstInterface amidstInterface, RandomAccessFile sharedMemoryFile, AreaLock lock) {
            this.amidstInterface = amidstInterface;
            this.sharedMemoryFile = sharedMemoryFile;
            this.lock = lock;

            try {
                byteBuffer = sharedMemoryFile.getChannel()
                        .map(FileChannel.MapMode.READ_WRITE, 0, Constants.TOTAL_BUFFER_SIZE)
                        .order(ByteOrder.LITTLE_ENDIAN);
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to map shared memory area", e);
            }
            initializeFileContents();
        }

        public void initializeFileContents() {
            this.requestHeader = buildHeader(Constants.REQUEST_AREA_START, Constants.REQUEST_AREA_HEADER_SIZE, byteBuffer);
            this.responseHeader = buildHeader(Constants.RESPONSE_AREA_START, Constants.RESPONSE_AREA_HEADER_SIZE, byteBuffer);
            requestHeader.mutateMessageNo(-1);
        }

        private Header buildHeader(int areaStart, int headerSize, ByteBuffer byteBuffer) {
            byteBuffer.position(areaStart);
            ByteBuffer headerBuffer = byteBuffer.slice().order(ByteOrder.LITTLE_ENDIAN);
            headerBuffer.limit(headerSize);
            FlatBufferBuilder builder = new FlatBufferBuilder(headerSize, FailingByteBufferFactory.INSTANCE, headerBuffer, Utf8.getDefault());
            builder.forceDefaults(true);
            int offset = Header.createHeader(builder, -1, 0);
            builder.finish(offset);
            headerBuffer.putInt(0, headerBuffer.position());
            return Header.getRootAsHeader(headerBuffer);
        }

        private void waitForClientAndHandleFirstRequest() {
            isRunning = true;
            while (doRun && requestHeader.messageNo() == -1) {
                try {
                    lock.lock();
                    if (requestHeader.messageNo() != -1) {
                        break;
                    }
                    LockSupport.parkNanos(20_000);
                    lock.unlock();
                } catch (InterruptedException e) {
                    shutdown();
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            try {
                handleRequest();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void run() {
            waitForClientAndHandleFirstRequest();
            int tries = 100;
            while (doRun) {
                try {
                    lock.lock();
                } catch (InterruptedException e) {
                    shutdown();
                    Thread.currentThread().interrupt();
                    return;
                }
                try {
                    if (requestHeader.messageNo() == previousRequestNo) {
                        if (tries-- <= 0) {
                            // we regained the lock without the request no increasing.
                            // this only happens if the client was closed. Shutdown this thread.
                            shutdown();
                            return;
                        }
                    } else {
                        handleRequest();
                        tries = 100;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                } finally {
                    lock.unlock();
                }
            }
        }

        private void handleRequest() {
            previousRequestNo = requestHeader.messageNo();

            byteBuffer.position(Constants.REQUEST_AREA_DATA_START);
            ByteBuffer requestBuffer = byteBuffer.slice().order(ByteOrder.LITTLE_ENDIAN);
            requestBuffer.limit(Constants.REQUEST_AREA_DATA_SIZE);
            requestBuffer.position(requestHeader.dataPos());

            Request.getRootAsRequest(requestBuffer, request);

            byteBuffer.position(Constants.RESPONSE_AREA_DATA_START);
            ByteBuffer responseBuffer = byteBuffer.slice();
            responseBuffer.limit(Constants.RESPONSE_AREA_DATA_SIZE);

            FlatBufferBuilder builder = new FlatBufferBuilder(Constants.RESPONSE_AREA_DATA_SIZE, FailingByteBufferFactory.INSTANCE, responseBuffer, Utf8.getDefault());
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
            builder.finish(responseOffset);
            responseHeader.mutateDataPos(responseBuffer.position());
            responseHeader.mutateMessageNo(responseHeader.messageNo() + 1);
        }

        private void shutdown() {
            doRun = false;
        }
    }

}
