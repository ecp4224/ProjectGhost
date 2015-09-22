package me.eddiep.ghost.matchmaking;

import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.game.ranking.Glicko2;
import me.eddiep.ghost.matchmaking.network.HttpServer;
import me.eddiep.ghost.matchmaking.network.TcpServer;
import me.eddiep.ghost.matchmaking.network.database.Database;
import me.eddiep.ghost.matchmaking.queue.PlayerQueue;
import me.eddiep.ghost.matchmaking.queue.impl.OriginalQueue;
import me.eddiep.ghost.network.validate.DummyValidator;
import me.eddiep.ghost.network.validate.LoginServerValidator;
import me.eddiep.ghost.network.validate.Validator;
import me.eddiep.ghost.utils.ArrayHelper;
import me.eddiep.ghost.utils.Global;
import me.eddiep.ghost.utils.Scheduler;

import java.util.HashMap;

public class Main {

    private static final HashMap<Queues, PlayerQueue> queues = new HashMap<>();

    private static final PlayerQueue[] playerQueues = {
            new OriginalQueue()
    };

    private static TcpServer server;
    private static HttpServer httpServer;
    public static Validator SESSION_VALIDATOR;

    public static void main(String[] args) {
        if (ArrayHelper.contains(args, "--offline")) {
            SESSION_VALIDATOR = new DummyValidator();
        } else {
            SESSION_VALIDATOR = new LoginServerValidator();
        }

        Scheduler.init();

        System.out.println("Setting up queues..");

        for (PlayerQueue queue : playerQueues) {
            queues.put(queue.queue(), queue);
        }

        System.out.println("Setting up database..");
        Database.setup();

        System.out.println("Starting matchmaking server...");

        httpServer = new HttpServer();
        httpServer.start();

        server = new TcpServer();
        server.start();

        System.out.println("Started!");

        Scheduler.scheduleRepeatingTask(new Runnable() {
            @Override
            public void run() {
                if (server.isRunning()) {
                    if (Glicko2.getInstance().updateRequired()) {
                        Glicko2.getInstance().performDailyUpdate();
                    }
                }
            }
        }, (60000 * 60) * 3);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("Match Saver");
                Database.processTimelineQueue(server);
            }
        }).start();

        System.out.println("Processing queues every " + (Global.QUEUE_MS_DELAY / 1000) + " seconds..");

        processQueues();
    }

    public static void processQueues() {
        while (server.isRunning()) {
            for (PlayerQueue queue : playerQueues) {
                if (queue == null) continue;

                queue.processQueue();
            }

            try {
                Thread.sleep(Global.QUEUE_MS_DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static PlayerQueue getQueueFor(Queues queue) {
        return queues.get(queue);
    }

    public static TcpServer getServer() {
        return server;
    }
}
