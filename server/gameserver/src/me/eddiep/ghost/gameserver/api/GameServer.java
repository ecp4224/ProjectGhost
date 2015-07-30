package me.eddiep.ghost.gameserver.api;

import me.eddiep.ghost.gameserver.api.game.Game;
import me.eddiep.ghost.gameserver.api.network.MatchFactory;
import me.eddiep.ghost.gameserver.api.network.MatchmakingClient;
import me.eddiep.ghost.gameserver.api.network.NetworkMatch;
import me.eddiep.ghost.gameserver.api.network.TcpUdpServer;
import me.eddiep.ghost.gameserver.api.network.packets.GameServerHeartbeat;
import me.eddiep.jconfig.JConfig;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class GameServer {

    private static Game game;
    private static TcpUdpServer server;
    private static GameServerConfig config;
    private static MatchmakingClient matchmakingClient;
    private static Thread heartbeatThread;

    public static Game getGame() {
        return game;
    }

    public static TcpUdpServer getServer() {
        return server;
    }

    public static GameServerConfig getConfig() {
        return config;
    }

    public static MatchmakingClient getMatchmakingClient() {
        return matchmakingClient;
    }

    public static Thread getHeartbeatThread() {
        return heartbeatThread;
    }

    public static void startServer(Game game) throws IOException {
        System.out.println("[SERVER] Reading config..");
        File file = new File("server.conf");
        GameServer.config = JConfig.newConfigObject(GameServerConfig.class);

        if (!file.exists()) {
            System.err.println("[SERVER] No config found! Saving default..");
            config.save(file);
            System.err.println("[SERVER] Please setup this server before running!");
            return;
        }

        config.load(file);

        System.out.println("[SERVER] Connecting to matchmaking server...");

        GameServer.server = new TcpUdpServer();

        Socket socket = new Socket(config.matchmakingIP(), config.matchmakingPort());

        GameServer.matchmakingClient = new MatchmakingClient(socket, server);
        GameServer.matchmakingClient.listen();
        try {
            matchmakingClient.auth(config.matchmakingSecret(), config.ID());
        } catch (InterruptedException e) {
            throw new IOException("Interrupted while waiting for OK from server!", e);
        }

        GameServer.game = game;

        System.out.println("[SERVER] Starting server..");

        server.start();

        game.onServerStart();

        System.out.println("[SERVER] Starting heartbeat..");

        GameServer.heartbeatThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (server.isRunning()) {
                    try {
                        sendHeardbeat();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        Thread.sleep(config.getHeartbeatInterval());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        heartbeatThread.start();

        System.out.println("[SERVER] All setup!");
    }

    public static void stopServer() throws IOException {
        if (game == null)
            throw new IllegalStateException("Server has not started!");

        System.out.println("Disconnecting from matchmaking server...");
        matchmakingClient.disconnect();

        matchmakingClient = null;

        System.out.println("Stopping server..");
        server.stop();
        game.onServerStop();

        server = null;
        game = null;

        System.out.println("Stopping heartbeat..");
        if (heartbeatThread.isAlive()) {
            heartbeatThread.interrupt();
            try {
                heartbeatThread.join(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        heartbeatThread = null;

        config = null;

        System.out.println("Server stopped!");
    }

    private static void sendHeardbeat() throws IOException {
        List<NetworkMatch> activeMatchlist = MatchFactory.INSTANCE.getAllActiveMatches();

        short matchCount = (short) activeMatchlist.size();
        short playerCount = (short) (matchCount * game.getPlayersPerMatch());
        long timePerTick = server.getTimePerTick();
        boolean isFull = matchCount >= config.getMaxMatchCount();

        GameServerHeartbeat packet = new GameServerHeartbeat(matchmakingClient);
        packet.writePacket(playerCount, matchCount, isFull, timePerTick);
    }
}
