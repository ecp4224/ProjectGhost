package com.boxtrotstudio.ghost.utils;

import java.util.LinkedList;
import java.util.Queue;

public class Scheduler {
    private static Thread scheduleThread;
    private static boolean running;
    private static Queue<Task> queue = new LinkedList<>();

    public static void init() {
        scheduleThread = new Thread(SCHEDULE_RUNNER);
        running = true;
        scheduleThread.start();
    }

    public static void shutdown() {
        running = false;
        scheduleThread.interrupt();
        try {
            scheduleThread.join();
        } catch (InterruptedException e) {
        }

        scheduleThread = null;
        queue.clear();
        queue = null;
    }

    public static CancelToken scheduleTask(Runnable runnable, long in) {
        CancelToken token = new CancelToken();
        Task task = new Task();
        task.cancelToken = token;
        task.runnable = runnable;
        task.executionTime = System.currentTimeMillis() + in;
        queue.offer(task);

        return token;
    }

    public static CancelToken scheduleRepeatingTask(Runnable runnable, long in) {
        CancelToken token = new CancelToken();
        Task task = new Task();
        task.cancelToken = token;
        task.runnable = runnable;
        task.executionTime = System.currentTimeMillis() + in;
        task.repeating = true;
        task.repeatTime = in;
        queue.offer(task);

        return token;
    }

    private static final Runnable SCHEDULE_RUNNER = new Runnable() {
        @Override
        public void run() {
            Queue<Task> temp = new LinkedList<>();
            while (running) {
                while (!queue.isEmpty()) {
                    Task task = queue.poll();
                    try {
                        if (task == null)
                            continue;
                        if (task.cancelToken != null && task.cancelToken.isCanceled())
                            continue;

                        long currentTime = System.currentTimeMillis();
                        if (task.executionTime <= currentTime) {
                            task.runnable.run();
                            if (task.repeating) {
                                task.executionTime = currentTime + task.repeatTime;
                                temp.offer(task);
                            }
                        } else {
                            temp.offer(task);
                        }
                    } catch (Throwable t) {
                        System.err.println("Error running task!");
                        t.printStackTrace();

                        if (task.repeating) {
                            task.executionTime = System.currentTimeMillis() + task.repeatTime;
                            temp.offer(task);
                        }
                    }
                }

                queue.addAll(temp);
                temp.clear();

                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            temp.clear();
        }
    };

    public static CancelToken scheduleTask(Runnable task) {
        return scheduleTask(task, 0L);
    }

    private static class Task {
        public long executionTime;
        public Runnable runnable;
        public boolean repeating;
        public long repeatTime;
        public CancelToken cancelToken;
    }
}
