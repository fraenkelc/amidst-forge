package amidst.remote.shm;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class FileLockTests {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    @Test
    public void testFileLock() throws InterruptedException, IOException {
        // GIVEN
        File lockFile = temporaryFolder.newFile();
        AreaLock lock = LockSource.createIPCLock(new RandomAccessFile(lockFile, "rw").getChannel());
        lock.lock();
        ProcessBuilder pb = TestUtils.exec(FailIfLockCanBeAcquired.class, Collections.emptyList(), Arrays.asList(lockFile.getAbsolutePath()));
        pb.inheritIO();

        // WHEN
        Process process = pb.start();
        assertEquals(-1, process.getInputStream().read());
        boolean exited = process.waitFor(100, TimeUnit.MILLISECONDS);

        // THEN
        assertFalse(exited);
        assertTrue(process.isAlive());
        process.destroyForcibly();
    }

    @Test
    public void testWaitForLock() throws InterruptedException, IOException {
        // GIVEN
        File lockFile = temporaryFolder.newFile();
        AreaLock lock = LockSource.createIPCLock(new RandomAccessFile(lockFile, "rw").getChannel());
        ProcessBuilder pb = TestUtils.exec(LockWithoutInput.class, Collections.emptyList(), Arrays.asList(lockFile.getAbsolutePath()));
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        Waiter waiter = new Waiter(lock);
        Thread waiterThread = new Thread(waiter);

        // WHEN
        Process process = pb.start();
        assertEquals(-1, process.getInputStream().read());
        process.waitFor(100, TimeUnit.MILLISECONDS);
        waiterThread.start();
        waiterThread.join(100);
        assertTrue(waiter.waiting);
        process.getOutputStream().write(0x10);
        process.getOutputStream().close();
        waiterThread.join(10);
        assertFalse(waiter.waiting);


        // THEN
        boolean exited = process.waitFor(1000, TimeUnit.MILLISECONDS);
        assertTrue(exited);
        assertEquals(0, process.exitValue());
    }

    private class Waiter implements Runnable {

        private final AreaLock lock;
        volatile boolean waiting = true;

        public Waiter(AreaLock lock) {
            this.lock = lock;
        }

        @Override
        public void run() {
            try {
                lock.lock();
            } catch (InterruptedException e) {
                errorCollector.addError(e);
                Thread.currentThread().interrupt();
            }
            waiting = false;
        }
    }


    public static class FailIfLockCanBeAcquired {
        public static void main(String[] args) throws FileNotFoundException, InterruptedException {
            File lockFile = new File(args[0]);
            AreaLock lock = LockSource.createIPCLock(new RandomAccessFile(lockFile, "rw").getChannel());
            System.out.close();
            lock.lock();
            System.exit(1);
        }
    }

    public static class LockWithoutInput {
        public static void main(String[] args) throws IOException, InterruptedException {
            File lockFile = new File(args[0]);
            AreaLock lock = LockSource.createIPCLock(new RandomAccessFile(lockFile, "rw").getChannel());
            System.out.close();
            lock.lock();
            try {
                int read = System.in.read();
                if (read == -1) {
                    fail("stdin is closed.");
                }
            } finally {
                lock.unlock();
            }
        }
    }

}
