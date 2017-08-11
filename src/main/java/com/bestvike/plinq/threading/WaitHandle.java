package com.bestvike.plinq.threading;

/**
 * Created by 许崇雷 on 2017/8/7.
 */
public interface WaitHandle extends AutoCloseable {
    void waitOne();

    void set();

    void reset();

    void close();
}
