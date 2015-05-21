package me.eddiep.ghost.central;

import com.google.gson.Gson;
import me.eddiep.ghost.central.network.dataserv.LoginServerBridge;
import me.eddiep.ghost.central.network.gameserv.GameServerFactory;
import me.eddiep.ghost.central.network.gameserv.QueueNamer;
import me.eddiep.ghost.central.ranking.Glicko2;

import java.io.IOException;
import java.util.Random;

public class Main {
    public static final Gson GSON = new Gson();
    public static final Random RANDOM = new Random();
    public static final TcpServer TCP_UDP_SERVER = new TcpServer();
    public static final long QUEUE_MS_DELAY = 10 * 1000; //10 seconds

    public static GameServerFactory gameServerFactory;

    private static LoginServerBridge loginServerBridge; //TODO Set this

    public static int random(int min, int max) {
        return RANDOM.nextInt(max - min) + min;
    }

    public static void main(String[] args) {
        System.out.println("Reading server config files..");

        gameServerFactory = new GameServerFactory();
        try {
            gameServerFactory.load();
            QueueNamer.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Connecting to GameServers...");
        gameServerFactory.connectToServers();

        System.out.println("Connected!");
        System.out.println("Starting tcp server..");

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
        System.out.println("Querying QueueInfo every " + (QUEUE_MS_DELAY / 1000) + " seconds..");

        while (TCP_UDP_SERVER.isRunning()) {
            gameServerFactory.queryQueueInfo();

            try {
                Thread.sleep(QUEUE_MS_DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static LoginServerBridge getLoginBridge() {
        return loginServerBridge;
    }
}
