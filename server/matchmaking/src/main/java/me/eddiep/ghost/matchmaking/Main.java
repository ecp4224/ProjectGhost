package me.eddiep.ghost.matchmaking;

import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.matchmaking.network.TcpServer;
import me.eddiep.ghost.matchmaking.queue.PlayerQueue;
import me.eddiep.ghost.matchmaking.queue.impl.OriginalQueue;
import me.eddiep.ghost.network.validate.DummyValidator;
import me.eddiep.ghost.network.validate.LoginServerValidator;
import me.eddiep.ghost.network.validate.Validator;
import me.eddiep.ghost.utils.ArrayHelper;
import me.eddiep.ghost.utils.Global;

import java.util.HashMap;

public class Main {

    private static final HashMap<Queues, PlayerQueue> queues = new HashMap<>();

    private static final PlayerQueue[] playerQueues = {
            new OriginalQueue()
    };

    private static TcpServer server;
    public static Validator SESSION_VALIDATOR;

    public static void main(String[] args) {
        System.out.println("Setting up SQL..");
        if (ArrayHelper.contains(args, "--offline")) {
            SESSION_VALIDATOR = new DummyValidator();
        } else {
            SESSION_VALIDATOR = new LoginServerValidator();
        }

        System.out.println("Setting up queues..");

        for (PlayerQueue queue : playerQueues) {
            queues.put(queue.queue(), queue);
        }

        System.out.println("Starting matchmaking server...");

        server = new TcpServer();
        server.start();

        System.out.println("Started!");

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
}
