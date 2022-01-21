package me.kutuzov.server.util;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class LoadingWheel {
    private String[] wheel;
    private int i;
    private Thread thread;

    public AtomicReference<String> status;
    public AtomicBoolean showing;
    public AtomicInteger delay;
    public LoadingWheel() {
        this.wheel = new String[]{"|", "/", "-", "\\"};
        this.i = 0;

        status = new AtomicReference<>("");
        showing = new AtomicBoolean(false);
        delay = new AtomicInteger(100);
        thread = new Thread(() -> {
            while(!thread.isInterrupted()) {
                if(showing.get()) {
                    ConsoleUtils.clearConsole();
                    System.out.println(status.get() + " " + next());
                }
                ConsoleUtils.trySleep(delay.get());
            }
        });
        thread.start();
    }

    public void stop() {
        thread.interrupt();
        thread.stop();
    }
    public void reset() { i=0; }
    public String next() {
        if(i == wheel.length)
            i=0;
        return wheel[i++];
    }
}