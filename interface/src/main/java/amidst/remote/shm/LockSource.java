package amidst.remote.shm;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.FileLockInterruptionException;
import java.util.concurrent.Semaphore;

public class LockSource {
    private LockSource() {
        // Utility Class
    }

    /**
     * Creates a File based lock for IPC use.
     * File locks are JVM based, so this can't be used to lock in the same JVM process
     *
     * @return
     */
    public static AreaLock createIPCLock(FileChannel channel) {
        return new AreaLock() {
            private FileLock fileLock;

            @Override
            public void lock() throws InterruptedException {
                try {
                    fileLock = channel.lock();
                } catch (FileLockInterruptionException e) {
                    throw new InterruptedException("Interrupted while acquiring lock");
                } catch (IOException e) {
                    throw new UncheckedIOException("Failed to acquire lock", e);
                }
            }

            @Override
            public void unlock() {
                try {
                    fileLock.release();
                    fileLock = null;
                } catch (IOException e) {
                    throw new UncheckedIOException("Failed to release lock", e);
                }
            }
        };
    }

    public static AreaLock createSemaphoreLock() {
        return new AreaLock() {
            private Semaphore semaphore = new Semaphore(1, true);

            @Override
            public void lock() throws InterruptedException {
                semaphore.acquire();
            }

            @Override
            public void unlock() {
                semaphore.release();
            }
        };
    }
}
