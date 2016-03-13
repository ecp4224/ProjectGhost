package me.eddiep.ghost.matchmaking;

import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.matchmaking.network.HttpServer;
import me.eddiep.ghost.matchmaking.network.TcpServer;
import me.eddiep.ghost.matchmaking.network.database.Database;
import me.eddiep.ghost.matchmaking.network.gameserver.Stream;
import me.eddiep.ghost.matchmaking.queue.PlayerQueue;
import me.eddiep.ghost.matchmaking.queue.impl.OriginalQueue;
import me.eddiep.ghost.matchmaking.queue.impl.RankedQueue;
import me.eddiep.ghost.network.validate.DummyValidator;
import me.eddiep.ghost.network.validate.LoginServerValidator;
import me.eddiep.ghost.network.validate.Validator;
import me.eddiep.ghost.utils.ArrayHelper;
import me.eddiep.ghost.utils.Global;
import me.eddiep.ghost.utils.Scheduler;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Main {

    private static final HashMap<Queues, HashMap<Stream, PlayerQueue>> queues = new HashMap<>();

    private static final Class[] playerQueuesTypes = {
            OriginalQueue.class,
            RankedQueue.class
    };

    private static List<PlayerQueue> queueList = new LinkedList<>();
    private static TcpServer server;
    private static HttpServer httpServer;
    public static Validator SESSION_VALIDATOR;

    public static int season = 0;

    public static void main(String[] args) {
        if (ArrayHelper.contains(args, "--offline")) {
            SESSION_VALIDATOR = new DummyValidator();
        } else {
            SESSION_VALIDATOR = new LoginServerValidator();
        }

        Scheduler.init();

        System.out.println("Setting up queues..");

        for (Class queueType : playerQueuesTypes) {
            Constructor<PlayerQueue> queueConstructor;
            try {
                queueConstructor = queueType.getConstructor(Stream.class);
            } catch (NoSuchMethodException e) {
                System.err.println("Could not make queue for type " + queueType.getCanonicalName());
                e.printStackTrace();
                continue;
            }
            Queues q = null;
            HashMap<Stream, PlayerQueue> temp = new HashMap<>();

            for (Stream stream : Stream.values()) {
                if (stream == Stream.BUFFERED)
                    continue;
                PlayerQueue queue = null;
                try {
                    queue = queueConstructor.newInstance(stream);
                } catch (InstantiationException e) {
                    System.err.println("Could not make queue for type " + queueType.getCanonicalName() + " for stream " + stream.name());
                    e.printStackTrace();
                    continue;
                } catch (IllegalAccessException e) {
                    System.err.println("Could not make queue for type " + queueType.getCanonicalName() + " for stream " + stream.name());
                    e.printStackTrace();
                    continue;
                } catch (InvocationTargetException e) {
                    System.err.println("Could not make queue for type " + queueType.getCanonicalName() + " for stream " + stream.name());
                    e.printStackTrace();
                    continue;
                }
                temp.put(stream, queue);
                queueList.add(queue);
                q = queue.queue();
            }
            if (q == null)
                continue;

            queues.put(q, temp);
        }

        System.out.println("Setting up database..");
        //Database.setup();

        System.out.println("Starting matchmaking server...");

        httpServer = new HttpServer();
        httpServer.start();

        server = new TcpServer();
        server.start();

        System.out.println("Started!");

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
            for (PlayerQueue queue : queueList) {
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

    public static PlayerQueue getQueueFor(Queues queue, Stream stream) {
        return queues.get(queue).get(stream);
    }

    public static TcpServer getServer() {
        return server;
    }
}
