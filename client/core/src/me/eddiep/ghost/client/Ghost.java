package me.eddiep.ghost.client;

import box2dLight.PointLight;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import me.eddiep.ghost.client.core.game.Characters;
import me.eddiep.ghost.client.core.logic.Handler;
import me.eddiep.ghost.client.core.physics.Physics;
import me.eddiep.ghost.client.core.physics.PhysicsImpl;
import me.eddiep.ghost.client.network.PlayerClient;
import me.eddiep.ghost.client.network.Stream;
import me.eddiep.ghost.client.utils.P2Runnable;
import me.eddiep.ghost.client.utils.Vector2f;
import org.apache.commons.cli.Options;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashMap;

public class Ghost {
    public static final AssetManager ASSETS = new AssetManager();
    public static final Physics PHYSICS = new PhysicsImpl();

    public static PlayerClient client;
    public static PlayerClient matchmakingClient;
    public static ArrayList<PointLight> lights = new ArrayList<>();
    public static boolean isInMatch, isReady, matchStarted;

    private static GhostClient INSTANCE;
    private static Handler DEFAULT = new BlankHandler();
    public static long latency;
    public static final long UPDATE_INTERVAL = 50L;
    public static boolean isSpectating;
    public static short PLAYER_ENTITY_ID;

    public static Options options;

    @NotNull
    public static String Session;

    public static boolean isOffline() {
        return options.hasOption("offline");
    }

    public static String getIp() {
        if (!options.hasOption("ip"))
            return "127.0.0.1";

        return options.getOption("ip").getValue();
    }

    public static Stream getStream() {
        for (Stream s : Stream.values()) {
            if (options.hasOption(s.name().toLowerCase()))
                return s;
        }

        return Stream.LIVE;
    }

    public static P2Runnable<Float, Float> onMatchFound;

    public static Characters selfCharacter;
    public static HashMap<String, Characters> enemies = new HashMap<>();
    public static HashMap<String, Characters> allies = new HashMap<>();

    public static void setDefaultHandler(Handler handler) {
        DEFAULT = handler;
    }

    public static GhostClient getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GhostClient(DEFAULT);
        }

        return INSTANCE;
    }

    private static boolean loaded;
    public static void loadGameAssets(AssetManager manager) {
        if (loaded)
            return;

        //Load all sprites
        FileHandle[] sprites = Gdx.files.internal("sprites").list(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith("png") ||
                        pathname.getName().endsWith("PNG") ||
                        pathname.getName().endsWith("jpg") ||
                        pathname.getName().endsWith("JPG");
            }
        });

        for (FileHandle file: sprites) {
            manager.load(file.path(), Texture.class);
        }

        FileHandle[] sounds = Gdx.files.internal("sounds").list(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith("mp3") ||
                       pathname.getName().endsWith("wav") ||
                       pathname.getName().endsWith("ogg");
            }
        });

        for (FileHandle file: sounds) {
            manager.load(file.path(), Sound.class);
        }


        //TODO Load other shit

        loaded = true;
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
