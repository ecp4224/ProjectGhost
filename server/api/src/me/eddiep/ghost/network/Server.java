package me.eddiep.ghost.network;

import java.io.IOException;

/**
 * Represents a Server that handles incoming packets and outgoing packets and handles the logic for them. Servers
 * sometimes have a ticker that executes every {@link me.eddiep.ghost.network.Server#getTickRate()} ms
 */
public abstract class Server {
    private boolean running;
    private long tickRate = 16;
    private Thread tickThread;
    private int tickNano;
    protected boolean debug;

    /**
     * Whether this server requires a ticker.
     * @return True if this server requires a tick, otherwise false
     */
    public abstract boolean requiresTick();

    /**
     * Execute a {@link java.lang.Runnable} next tick
     * @param runnable The runnable to execute
     */
    public abstract void executeNextTick(Runnable runnable);


    public boolean isDebugMode() {
        return debug;
    }

    public void setDebugMode(boolean val) {
        this.debug = val;
    }

    /**
     * Start this server.
     */
    public final void start() {
        onStart();
        if (!running) {
            throw new IllegalStateException("super.onStart() was not invoked!");
        }
    }

    /**
     * Stop this server
     */
    public final void stop() {
        onStop();
        if (running) {
            throw new IllegalStateException("super.onStop() was not invoked!");
        }
    }

    /**
     * This method is invoked when {@link me.eddiep.ghost.network.Server#start()} is invoked. <b>super.onStart()</b> should
     * always be invoked!
     */
    protected void onStart() {
        running = true;
        if (requiresTick()) {
            tickThread = new Thread(TICK_RUNNABLE);
            tickThread.start();
        }
    }

    /**
     * This method is invoked when {@link me.eddiep.ghost.network.Server#stop()} is invoked. <b>super.onStop()</b> should
     * always be invoked!
     */
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

    /**
     * Whether this server is running or not
     * @return True if this server is running, false otherwise
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Log something as the server
     * @param message The message to log
     */
    protected void log(String message) {
        System.out.println("[SERVER] " + message);
    }

    /**
     * This method is invoked when a tick occurs
     */
    protected void onTick() {

    }

    /**
     * Start a {@link java.lang.Runnable} process in another {@link java.lang.Thread}
     * @param runnable The runnable to execute
     */
    protected void runInBackground(Runnable runnable) {
        new Thread(runnable).start();
    }

    /**
     * This method is invoked when a {@link me.eddiep.ghost.network.Client} disconnects
     * @param client The client that disconnected
     * @throws IOException If there was a problem disconnecting the client
     */
    public void disconnect(Client client) throws IOException {

    }

    /**
     * How often this server's ticker ticks
     * @return The tick rate in ms
     */
    public long getTickRate() {
        return tickRate;
    }

    /**
     * Set how often this server's ticker ticks
     * @param tickRate The tick rate in ms
     */
    protected void setTickRate(long tickRate) {
        this.tickRate = tickRate;
    }

    /**
     * The precision for this server's ticker
     * @return How long the server's ticker will additionally wait in nanoseconds for the next tick
     */
    public int getTickNanos() { return tickNano; }

    /**
     * Set the precision for this server's ticker
     * @param tickNanos How long the server's ticker will additionally wait in nanoseconds for the next tick
     */
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
