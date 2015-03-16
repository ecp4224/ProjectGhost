package me.eddiep.ghost.server;

import me.eddiep.ghost.server.game.queue.PlayerQueue;
import me.eddiep.ghost.server.game.queue.QueueType;

import java.util.Random;
import java.util.UUID;

public class Main {
    public static final Random RANDOM = new Random();
    public static final HttpServer HTTP_SERVER = new HttpServer();
    public static final TcpUdpServer TCP_UDP_SERVER = new TcpUdpServer();
    public static final long QUEUE_MS_DELAY = 10 * 1000; //10 seconds

    public static int random(int min, int max) {
        return RANDOM.nextInt(max - min) + min;
    }

    public static void main(String[] args) {
        System.out.println("Starting http server..");
        HTTP_SERVER.start();
        System.out.println("Started!");
        System.out.println("Starting tcp/udp server..");
        TCP_UDP_SERVER.start();
        System.out.println("Started!");

        System.out.println("Setting up Queue System");
        PlayerQueue[] queues = new PlayerQueue[QueueType.values().length];
        for (int i = 0; i < QueueType.values().length; i++) {
            System.out.println("Init " + QueueType.values()[i].name());
            queues[i] = QueueType.values()[i].getQueue();
        }

        System.out.println("Processing queues every " + (QUEUE_MS_DELAY / 1000) + " seconds..");
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
