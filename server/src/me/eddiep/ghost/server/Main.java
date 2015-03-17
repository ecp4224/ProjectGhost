package me.eddiep.ghost.server;

import me.eddiep.ghost.server.game.queue.PlayerQueue;
import me.eddiep.ghost.server.game.queue.QueueType;
import me.eddiep.ghost.server.network.sql.SQL;
import me.eddiep.jconfig.JConfig;

import java.io.File;
import java.util.Random;
import java.util.UUID;

public class Main {
    public static final Random RANDOM = new Random();
    public static final HttpServer HTTP_SERVER = new HttpServer();
    public static final TcpUdpServer TCP_UDP_SERVER = new TcpUdpServer();
    public static final long QUEUE_MS_DELAY = 10 * 1000; //10 seconds

    public static SQL SQL;

    public static int random(int min, int max) {
        return RANDOM.nextInt(max - min) + min;
    }

    public static void main(String[] args) {
        System.out.println("Reading server config..");

        ServerConfig conf = readConfig();

        System.out.println("Done!");
        System.out.println("Connecting to SQL");

        setupSQL(conf);

        System.out.println("Connected!");
        System.out.println("Starting http server..");

        HTTP_SERVER.start();

        System.out.println("Started!");
        System.out.println("Starting tcp/udp server..");

        TCP_UDP_SERVER.start();

        System.out.println("Started!");
        System.out.println("Setting up Queue System");

        PlayerQueue[] queues = initQueue();

        System.out.println("Processing queues every " + (QUEUE_MS_DELAY / 1000) + " seconds..");

        processQueues(queues);
    }

    private static ServerConfig readConfig() {
        ServerConfig config = JConfig.newConfigObject(ServerConfig.class);
        File file = new File("server.conf");
        if (!file.exists()) {
            config.save(file);
        } else {
            config.load(file);
        }
        return config;
    }

    private static void setupSQL(ServerConfig config) {
        try {
            SQL = (me.eddiep.ghost.server.network.sql.SQL) Class.forName(config.getSQLDriver()).newInstance();
            SQL.loadAndSetup();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static PlayerQueue[] initQueue() {
        PlayerQueue[] queues = new PlayerQueue[QueueType.values().length];
        for (int i = 0; i < QueueType.values().length; i++) {
            System.out.println("Init " + QueueType.values()[i].name());
            queues[i] = QueueType.values()[i].getQueue();
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
