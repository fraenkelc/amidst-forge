package amidst.remote.shm;

public interface AreaLock {
    void lock() throws InterruptedException;

    void unlock();

}
