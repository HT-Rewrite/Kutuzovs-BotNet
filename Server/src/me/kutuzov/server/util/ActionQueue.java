package me.kutuzov.server.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

public class ActionQueue {
    private BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

    public ActionQueue() {
        new Thread(() -> {
            while (true) {
                try {
                    queue.take().run();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }

            System.out.println("WARNING! Packet queue has been stopped!");
        }).start();
    }

    public void add(Runnable runnable) {
        queue.offer(runnable);
    }
    public void addWait(Runnable action) {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            queue.offer(() -> {
                action.run();
                latch.countDown();
            });
            latch.await();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public BlockingQueue<Runnable> getQueue() {
        return queue;
    }
}