package me.eddiep.ghost.central;

import java.net.DatagramPacket;

public abstract class Server {
    private boolean running;
    private long tickRate = 16;
    private Thread tickThread;
    private int tickNano;

    public abstract boolean requiresTick();

    public final void start() {
        onStart();
        if (!running) {
            throw new IllegalStateException("super.onStart() was not invoked!");
        }
    }

    public final void stop() {
        onStop();
        if (running) {
            throw new IllegalStateException("super.onStop() was not invoked!");
        }
    }

    protected void onStart() {
        running = true;
        if (requiresTick()) {
            tickThread = new Thread(TICK_RUNNABLE);
            tickThread.start();
        }
    }

    protected void onStop() {
        running = false;
        if (tickThread != null) {
            tickThread.interrupt();
            try {
                tickThread.join(15000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isRunning() {
        return running;
    }

    protected void log(String message) {
        System.out.println("[SERVER] " + message);
    }

    protected void onTick() {

    }

    protected void runInBackground(Runnable runnable) {
        new Thread(runnable).start();
    }

    public long getTickRate() {
        return tickRate;
    }

    protected void setTickRate(long tickRate) {
        this.tickRate = tickRate;
    }

    public int getTickNanos() { return tickNano; }

    protected void setTickNanos(int tickNanos) { this.tickNano = tickNanos; }

    private final Runnable TICK_RUNNABLE = new Runnable() {
        @Override
        public void run() {
            while (running) {
                try {
                    onTick();
                } catch (Throwable t) {
                    t.printStackTrace();
                }

                try {
                    Thread.sleep(tickRate, tickNano);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };
}
