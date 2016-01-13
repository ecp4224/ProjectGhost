package me.eddiep.ghost.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import me.eddiep.ghost.client.core.physics.Physics;
import me.eddiep.ghost.client.core.physics.PhysicsImpl;
import me.eddiep.ghost.client.network.PlayerClient;
import me.eddiep.ghost.client.utils.P2Runnable;
import me.eddiep.ghost.client.utils.Vector2f;

import java.io.File;
import java.io.FileFilter;

public class Ghost {
    public static final AssetManager ASSETS = new AssetManager();
    public static final Physics PHYSICS = new PhysicsImpl();

    public static PlayerClient client;
    public static boolean isInMatch, isReady, matchStarted;

    private static GhostClient INSTANCE;
    private static Handler DEFAULT = new BlankHandler();
    public static long latency;
    public static final long UPDATE_INTERVAL = 50L;
    public static boolean isSpectating;
    public static short PLAYER_ENTITY_ID;

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

    public static void loadGameAssets(AssetManager manager) {
        //Load all sprites
        FileHandle[] files = Gdx.files.internal("sprites").list(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith("png") ||
                        pathname.getName().endsWith("PNG") ||
                        pathname.getName().endsWith("jpg") ||
                        pathname.getName().endsWith("JPG");
            }
        });

        for (FileHandle file : files) {
            manager.load(file.path(), Texture.class);
        }

        //TODO Load other shit
    }

    private static long lastPingCheck;
    private static long startPing;
    private static Vector2f lastTarget;
    private static boolean checkPing;
    public static void startPingTimer(Vector2f target) {
        if (lastPingCheck + 5000 >= System.currentTimeMillis())
            return;

        lastTarget = target;
        startPing = System.nanoTime();
        lastPingCheck = System.currentTimeMillis();
        checkPing = true;
    }

    public static void endPingTimer(Vector2f target) {
        if (!checkPing)
            return;

        if ((lastTarget == null && target != null) || lastTarget.x != target.x || lastTarget.y != target.y) {
            long ping = System.nanoTime() - startPing;
            ping /= 2;
            ping /= 1000000;

            Ghost.latency = ping;
            checkPing = false;
        }
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
