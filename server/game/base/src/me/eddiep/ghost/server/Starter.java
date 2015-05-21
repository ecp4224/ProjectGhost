package me.eddiep.ghost.server;

import com.google.gson.Gson;
import me.eddiep.ghost.server.game.Game;
import me.eddiep.ghost.server.network.dataserv.LoginServerBridge;

public class Starter {
    public static final Gson GSON = new Gson();

    private static LoginServerBridge loginServerBridge;
    private static Game game;
    private static TcpUdpServer server;


    public static void startServer(LoginServerBridge loginServerBridge, Game game) {
        Starter.loginServerBridge = loginServerBridge;
        Starter.game = game;

        System.out.println("Starting game server..");

        server = new TcpUdpServer();
        server.start();

        System.out.println("Loading game...");

        game.onStart();

        System.out.println("Processing queues..");

        while (server.isRunning()) {
            game.playerQueueProcessor().processQueue();
            game.onQueueProcessed();

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static Game getGame() { return game; }

    public static TcpUdpServer getTcpUdpServer() {
        return server;
    }

    public static LoginServerBridge getLoginBridge() {
        return loginServerBridge;
    }
}
