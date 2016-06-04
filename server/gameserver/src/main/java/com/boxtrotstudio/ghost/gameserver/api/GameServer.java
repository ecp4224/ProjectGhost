package com.boxtrotstudio.ghost.gameserver.api;

import com.boxtrotstudio.ghost.common.game.MatchFactory;
import com.boxtrotstudio.ghost.common.game.NetworkMatch;
import com.boxtrotstudio.ghost.common.game.PlayerFactory;
import com.boxtrotstudio.ghost.common.network.BaseServer;
import com.boxtrotstudio.ghost.game.queue.Queues;
import com.boxtrotstudio.ghost.gameserver.api.game.player.GameServerPlayerFactory;
import com.boxtrotstudio.ghost.gameserver.api.network.MatchmakingClient;
import com.boxtrotstudio.ghost.gameserver.api.network.impl.BasicMatchFactory;
import com.boxtrotstudio.ghost.gameserver.api.network.packets.GameServerHeartbeat;
import com.boxtrotstudio.ghost.gameserver.common.*;
import com.boxtrotstudio.ghost.utils.CancelToken;
import com.boxtrotstudio.ghost.utils.Global;
import com.boxtrotstudio.ghost.utils.Scheduler;
import me.eddiep.jconfig.JConfig;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class GameServer {

    private static BaseServer server;
    private static GameServerConfig config;
    private static MatchmakingClient matchmakingClient;
    public static Stream currentStream;
    private static CancelToken heartbeatTask;

    public static BaseServer getServer() {
        return server;
    }

    public static GameServerConfig getConfig() {
        return config;
    }

    public static MatchmakingClient getMatchmakingClient() {
        return matchmakingClient;
    }

    public static void startServer() throws IOException {
        System.out.println("[SERVER] Setting up games..");

        GameFactory.addGame(Queues.RANKED, new RankedGame());
        GameFactory.addGame(Queues.TWO_V_TWO, new Casual2v2Game());
        GameFactory.addGame(Queues.WEAPONSELECT, new CasualGame());
        GameFactory.addGame(Queues.ORIGINAL, new TestGame());

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

        if (config.matchmakingSecret().length() != 32) {
            System.err.println("Provided secret is not 32 characters!");
            System.err.println("Aborting..");
            System.exit(1);
            return;
        }

        Scheduler.init();

        MatchFactory.setMatchCreator(new BasicMatchFactory());
        PlayerFactory.setPlayerCreator(GameServerPlayerFactory.INSTANCE);

        System.out.println("[SERVER] Connecting to matchmaking server...");

        GameServer.server = new BaseServer(config);
        Global.DEFAULT_SERVER = GameServer.server;

        Socket socket = new Socket(config.matchmakingIP(), config.matchmakingPort());

        GameServer.matchmakingClient = new MatchmakingClient(socket, server);
        GameServer.matchmakingClient.listen();
        try {
            matchmakingClient.auth(config.matchmakingSecret(), config.ID());
        } catch (InterruptedException e) {
            throw new IOException("Interrupted while waiting for OK from server!", e);
        }

        server.start();

        System.out.println("[SERVER] Starting heartbeat..");

        heartbeatTask = Scheduler.scheduleRepeatingTask(new Runnable() {
            @Override
            public void run() {
                if (server.isRunning()) {
                    try {
                        sendHeardbeat();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, config.getHeartbeatInterval());

        System.out.println("[SERVER] All setup!");
    }

    public static void stopServer() throws IOException {
        System.out.println("Disconnecting from matchmaking server...");
        matchmakingClient.disconnect();

        matchmakingClient = null;

        System.out.println("Stopping server..");
        server.stop();

        server = null;

        GameFactory.shutdown();

        System.out.println("Stopping heartbeat..");
        heartbeatTask.cancel();
        heartbeatTask = null;

        config = null;

        System.out.println("Server stopped!");
    }

    private static void sendHeardbeat() throws IOException {
        if (!matchmakingClient.isConnected()) {
            //Attempt to reconnect..
            System.err.println("Attempting to reconnect to matchmaking server..");
            try {
                Socket socket = new Socket(config.matchmakingIP(), config.matchmakingPort());

                GameServer.matchmakingClient.dispose();

                GameServer.matchmakingClient = new MatchmakingClient(socket, server);
                GameServer.matchmakingClient.listen();

                System.err.println("Connected! Sending auth packet..");

                try {
                    matchmakingClient.auth(config.matchmakingSecret(), config.ID());
                } catch (InterruptedException e) {
                    throw new IOException("Interrupted while waiting for OK from server!", e);
                }
            } catch (IOException e) {
                System.err.println("Reconnect failed! (" + e.getMessage() + ")");
            }
            return;
        }

            List<NetworkMatch> activeMatchlist = MatchFactory.getCreator().getAllActiveMatches();

            short matchCount = (short) activeMatchlist.size();
            short playerCount = (short) (server.getClientCount());
            long timePerTick = 0L; //TODO Get average from worlds
            boolean isFull = matchCount >= config.getMaxMatchCount();

            GameServerHeartbeat packet = new GameServerHeartbeat(matchmakingClient);
            packet.writePacket(playerCount, matchCount, isFull, timePerTick);

    }
}
