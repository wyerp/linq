package com.bestvike.linq;

import com.bestvike.plinq.threading.AutoResetEvent;
import com.bestvike.plinq.threading.ManualResetEvent;
import org.junit.Test;

/**
 * Created by 许崇雷 on 2017/8/8.
 */
public class ThreadingTest {
  private volatile  int count;
    @Test
    public void testAutoEvent() {
        AutoResetEvent eve = new AutoResetEvent(false);

        for (int i = 0; i < 10; i++) {
            Thread t = new Thread(() ->
            {
                System.out.println(++this.count);
                eve.waitOne();
                System.out.println("i got it:" + Thread.currentThread().getId());
            });
            t.start();
        }

        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    @Test
    public void testEvent2() {
        ManualResetEvent eve = new ManualResetEvent(false);

        for (int i = 0; i < 10; i++) {
            Thread t = new Thread(() ->
            {
                System.out.println(++this.count);
                eve.waitOne();
                System.out.println("i got it:" + Thread.currentThread().getId());
            });
            t.start();
        }

        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
