package me.eddiep.ghost.test;

import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.game.ranking.Glicko2;
import me.eddiep.ghost.network.sql.impl.OfflineDB;
import me.eddiep.ghost.test.game.queue.PlayerQueue;
import me.eddiep.ghost.test.game.queue.impl.ChooseWeaponQueue;
import me.eddiep.ghost.test.game.queue.impl.LaserQueue;
import me.eddiep.ghost.test.game.queue.impl.OriginalQueue;
import me.eddiep.ghost.test.network.HttpServer;
import me.eddiep.ghost.test.network.TcpUdpServer;
import me.eddiep.ghost.utils.ArrayHelper;
import me.eddiep.jconfig.JConfig;

import java.io.File;
import java.util.HashMap;

import static me.eddiep.ghost.utils.Global.QUEUE_MS_DELAY;
import static me.eddiep.ghost.utils.Global.SQL;


public class Main {
    public static final HttpServer HTTP_SERVER = new HttpServer();
    public static final TcpUdpServer TCP_UDP_SERVER = new TcpUdpServer();
    public static boolean OFFLINE;

    public static HashMap<Queues, PlayerQueue> playerQueueHashMap = new HashMap<>();

    private static Class[] TO_INIT = {
            OriginalQueue.class,
            LaserQueue.class,
            ChooseWeaponQueue.class
    };

    public static void main(String[] args) {
        if (ArrayHelper.contains(args, "--offline")) {
            SQL = new OfflineDB();
            SQL.loadAndSetup();
            OFFLINE = true;
        }

        System.out.println("Reading test config..");

        ServerConfig conf = readConfig();

        System.out.println("Done!");

        if (!OFFLINE) {
            System.out.println("Connecting to SQL");

            setupSQL(conf);

            System.out.println("Connected!");
        }

        System.out.println("Starting http test..");

        HTTP_SERVER.start();

        System.out.println("Started!");
        System.out.println("Starting tcp/udp test..");

        TCP_UDP_SERVER.start();

        System.out.println("Started!");
        System.out.println("Setting up Rank System");

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (TCP_UDP_SERVER.isRunning()) {
                    if (Glicko2.getInstance().updateRequired()) {
                        Glicko2.getInstance().performDailyUpdate();
                    }
                    try {
                        Thread.sleep((60000 * 60) * 3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        System.out.println("Started!");
        System.out.println("Setting up Queue System");

        PlayerQueue[] queues = initQueue();

        System.out.println("Processing queues every " + (QUEUE_MS_DELAY / 1000) + " seconds..");

        processQueues(queues);
    }

    private static ServerConfig readConfig() {
        ServerConfig config = JConfig.newConfigObject(ServerConfig.class);
        File file = new File("test.conf");
        if (!file.exists()) {
            config.save(file);
        } else {
            config.load(file);
        }
        return config;
    }

    private static void setupSQL(ServerConfig config) {
        try {
            SQL = (me.eddiep.ghost.network.sql.SQL) Class.forName(config.getSQLDriver()).newInstance();
            SQL.loadAndSetup();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static PlayerQueue[] initQueue() {
        PlayerQueue[] queues = new PlayerQueue[TO_INIT.length];
        for (int i = 0; i < queues.length; i++) {
            try {
                Class class_ = TO_INIT[i];
                PlayerQueue queue = (PlayerQueue) class_.newInstance();
                System.out.println("Init " + queue.queue().name());
                queues[i] = queue;
                playerQueueHashMap.put(queue.queue(), queue);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return queues;
    }

    private static void processQueues(PlayerQueue[] queues) {
        while (TCP_UDP_SERVER.isRunning()) {

            for (PlayerQueue queue : queues) {
                if (queue == null) continue;

                queue.processQueue();
            }

            try {
                Thread.sleep(QUEUE_MS_DELAY);
            } catch (InterruptedException ignored) { }
        }
    }
}
