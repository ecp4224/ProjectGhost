package com.boxtrotstudio.ghost.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.boxtrotstudio.ghost.client.core.logic.Handler;
import com.boxtrotstudio.ghost.client.core.physics.Physics;
import com.boxtrotstudio.ghost.client.network.Stream;
import com.boxtrotstudio.ghost.client.utils.P2Runnable;
import com.boxtrotstudio.ghost.client.utils.Vector2f;
import com.sun.javafx.beans.annotations.NonNull;
import com.boxtrotstudio.ghost.client.core.physics.PhysicsImpl;
import com.boxtrotstudio.ghost.client.core.render.LightCreator;
import com.boxtrotstudio.ghost.client.network.PlayerClient;
import org.apache.commons.cli.Options;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

public class Ghost {
    public static final AssetManager ASSETS = new AssetManager();
    public static final Physics PHYSICS = new PhysicsImpl();

    public static PlayerClient client;
    public static PlayerClient matchmakingClient;
    public static ArrayList<LightCreator> lights = new ArrayList<>();
    public static boolean isInMatch, isReady, matchStarted;

    private static GhostClient INSTANCE;
    private static Handler DEFAULT = new BlankHandler();
    public static long latency;
    public static final long UPDATE_INTERVAL = 50L;
    public static boolean isSpectating;
    public static short PLAYER_ENTITY_ID;

    public static Options options;

    @NonNull
    public static String Session;

    public static boolean isOffline() {
        return options.hasOption("offline");
    }

    public static boolean isSSLDisabled() {
        return options != null && options.hasOption("nossl");
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


        if (isSSLDisabled())
            disableCertificateValidation();
        else {
            try {
                addRootCA();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

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

        //Load all sprites
        FileHandle[] map_files = Gdx.files.internal("maps").list(new FileFilter() {
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

        for (FileHandle file: map_files) {
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

    private static void addRootCA() throws Exception {
        if (isSSLDisabled())
            return;

        FileHandle x1File = Gdx.files.internal("cert/lets-encrypt-x1-cross-signed.der");
        FileHandle x2File = Gdx.files.internal("cert/lets-encrypt-x2-cross-signed.der");
        InputStream fis2 = x1File.read();
        InputStream fis3 = x2File.read();

        Certificate x1CA = CertificateFactory.getInstance("X.509").generateCertificate(fis2);
        Certificate x2CA = CertificateFactory.getInstance("X.509").generateCertificate(fis3);

        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        ks.setCertificateEntry(Integer.toString(1), x1CA);
        ks.setCertificateEntry(Integer.toString(2), x2CA);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, tmf.getTrustManagers(), null);

        HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
    }

    public static void disableCertificateValidation() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }};

        // Ignore differences between given hostname and certificate hostname
        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) { return true; }
        };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(hv);
        } catch (Exception e) {}
    }
}