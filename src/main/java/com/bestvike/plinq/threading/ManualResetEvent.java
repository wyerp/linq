package com.bestvike.plinq.threading;

/**
 * Created by 许崇雷 on 2017/8/7.
 */
public class ManualResetEvent implements WaitHandle {
    private volatile boolean state;
    private final Object lock = new Object();

    public ManualResetEvent(boolean initialState) {
        this.state = initialState;
    }

    @Override
    public void waitOne() {
        synchronized (this.lock) {
            try {
                while (!this.state)
                    this.lock.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void set() {
        synchronized (this.lock) {
            this.state = true;
            this.lock.notifyAll();
        }
    }

    @Override
    public void reset() {
        synchronized (this.lock) {
            this.state = false;
        }
    }

    @Override
    public void close() {
    }
}
