package com.boxtrotstudio.ghost.client.utils;

import com.badlogic.gdx.utils.TimeUtils;

public class Timer {
    private CancelToken cancelToken;
    private PFunction<Long, Boolean> runnable;
    private Thread timerThread;
    private long pauseDuration;
    private long startTime;

    public static CancelToken newTimer(Runnable runnable, long pauseDuration) {
        Timer timer = new Timer(val -> {
            runnable.run();
            return true;
        }, pauseDuration);
        timer.start();
        return timer.cancelToken;
    }

    public static CancelToken newTimer(PFunction<Long, Boolean> runnable, long pauseDuration) {
        Timer timer = new Timer(runnable, pauseDuration);
        timer.start();
        return timer.cancelToken;
    }

    public Timer(PFunction<Long, Boolean> runnable, long pauseDuration) {
        this.runnable = runnable;
        this.pauseDuration = pauseDuration;
        cancelToken = new CancelToken();
        createThread();
    }

    private void createThread() {
        timerThread = new Thread(new TimerRunnable());
    }

    public void start() {
        startTime = TimeUtils.millis();
        timerThread.start();
    }

    public void stop() {
        cancelToken.cancel();
        timerThread.interrupt();
    }

    public void stopAndJoin(long timeout) {
        cancelToken.cancel();
        timerThread.interrupt();
        try {
            timerThread.join(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public long getPauseDuration() {
        return pauseDuration;
    }

    public CancelToken getCancelToken() {
        return cancelToken;
    }

    private class TimerRunnable implements Runnable {

        @Override
        public void run() {
            while (!cancelToken.isCanceled()) {
                long duration = TimeUtils.millis() - startTime;
                boolean shouldContinue = runnable.run(duration);

                if (!shouldContinue)
                    cancelToken.cancel();

                try {
                    Thread.sleep(pauseDuration);
                } catch (InterruptedException ignored) { }
            }
        }
    }
}
