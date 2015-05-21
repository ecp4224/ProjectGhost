package me.eddiep.ghost.central.utils;

public class TimeUtils {
    public static void executeIn(final long ms, final Runnable runnable) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(ms);
                    runnable.run();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }).start();
    }
}
