package me.eddiep.ghost.utils;

/**
 * A utility class for timing
 */
public class TimeUtils {

    /**
     * Execute a {@link java.lang.Runnable} after waiting <b>ms</b> milliseconds. This function will create a new
     * {@link java.lang.Thread} and <b>will not be ran inside the server tick</b>
     * @param ms How long to wait before execution
     * @param runnable The {@link java.lang.Runnable} to execute
     */
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

    /**
     * Execute a {@link java.lang.Runnable} while a condition is true. This function will create a new
     * {@link java.lang.Thread} and <b>will not be ran inside the server tick</b>
     * @param runnable The {@link java.lang.Runnable} to execute
     * @param condition The condition that should true
     * @param sleep How long to sleep between each execution
     */
    public static void executeWhile(final Runnable runnable, final PFunction<Void, Boolean> condition, final long sleep) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (condition.run(null)) {
                    runnable.run();

                    try {
                        Thread.sleep(sleep);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * Execute a {@link java.lang.Runnable} <b>until</b> a condition is true. This function will create a new
     * {@link java.lang.Thread} and <b>will not be ran inside the server tick</b>
     * @param runnable The {@link java.lang.Runnable} to execute
     * @param condition The condition that should true
     * @param sleep How long to sleep between each execution
     */
    public static void executeUntil(final Runnable runnable, final PFunction<Void, Boolean> condition, final long sleep) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!condition.run(null)) {
                    runnable.run();

                    try {
                        Thread.sleep(sleep);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    
    /**
     * Execute a runnable when a condition is met on the main server tick. The condition is checked every server tick
     * and when the condition returns true, the runnable is ran. 
     * @param runnable The runnable to run
     * @param condition The condition that must be true to run the condition
     * @param server The server this runnable will run on
     */
    public static void executeWhen(final Runnable runnable, final PFunction<Void, Boolean> condition, Server server) {
        Runnable toRun = new Runnable() {
            @Override
            public void run() {
                if (condition.run(null)) {
                    runnable.run();
                } else {
                    server.executeNextTick(this);
                }
            }
        };
        
        server.executeNextTick(toRun);
    }

    //Code taken from: https://code.google.com/p/replicaisland/source/browse/trunk/src/com/replica/replicaisland/Lerp.java?r=5
    //Because I'm a no good dirty scrub
    public static float ease(float start, float target, float duration, float timeSinceStart) {
        float value = start;
        if (timeSinceStart > 0.0f && timeSinceStart < duration) {
            final float range = target - start;
            final float percent = timeSinceStart / (duration / 2.0f);
            if (percent < 1.0f) {
                value = start + ((range / 2.0f) * percent * percent * percent);
            } else {
                final float shiftedPercent = percent - 2.0f;
                value = start + ((range / 2.0f) *
                        ((shiftedPercent * shiftedPercent * shiftedPercent) + 2.0f));
            }
        } else if (timeSinceStart >= duration) {
            value = target;
        }
        return value;
    }
}
