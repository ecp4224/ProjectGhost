package me.eddiep.ghost.client;

import me.eddiep.ghost.client.utils.P2Runnable;

public class Ghost {
    public static boolean isInMatch, isReady, matchStarted;

    private static GhostClient INSTANCE;
    private static Handler DEFAULT = new BlankHandler();
    public static long latency;
    public static final long UPDATE_INTERVAL = 50L;
    public static boolean isSpectating;

    public static P2Runnable<Float, Float> onMatchFound;

    public static void setDefaultHandler(Handler handler) {
        DEFAULT = handler;
    }

    public static GhostClient getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GhostClient(DEFAULT);
        }

        return INSTANCE;
    }

    private static class BlankHandler implements Handler {
        @Override
        public void start() {

        }

        @Override
        public void tick() {

        }
    }
}
