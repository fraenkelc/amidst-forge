package amidst.remote.shm;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import static org.junit.Assert.assertTrue;

public class LockSourceTests {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    @Test
    public void testSemaphoreLock() throws InterruptedException {
        // GIVEN
        AreaLock lock = LockSource.createSemaphoreLock();
        lock.lock();
        Thread t = new Thread(() -> failIfLockCanBeAcquired(lock));
        t.setDaemon(true);

        // WHEN
        t.start();
        t.join(100);

        // THEN
        assertTrue(t.isAlive());
    }

    private void failIfLockCanBeAcquired(AreaLock lock) {
        try {
            lock.lock();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        errorCollector.addError(new AssertionError("Lock should not be aquirable in this thread."));
    }


    public static class FileLockPeer {
        public static void main(String... args) {

        }
    }
}
