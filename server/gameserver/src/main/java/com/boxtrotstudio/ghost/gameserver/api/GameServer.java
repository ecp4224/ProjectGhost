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
import com.boxtrotstudio.ghost.utils.WebUtils;
import me.eddiep.jconfig.JConfig;
import me.eddiep.ubot.UBot;
import me.eddiep.ubot.module.impl.HttpVersionFetcher;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.List;

public class GameServer {

    private static BaseServer server;
    private static GameServerConfig config;
    private static MatchmakingClient matchmakingClient;
    public static Stream currentStream;
    private static CancelToken heartbeatTask;
    private static me.eddiep.ubot.utils.CancelToken ubotToken;
    private static UBot uBot;

    public static BaseServer getServer() {
        return server;
    }

    public static GameServerConfig getConfig() {
        return config;
    }

    public static MatchmakingClient getMatchmakingClient() {
        return matchmakingClient;
    }

    public static void startServer() throws Exception {
        WebUtils.trustLetsEncrypt();

        System.out.println("[PRE-INIT] Setting up games..");

        GameFactory.addGame(Queues.RANKED, new RankedGame());
        GameFactory.addGame(Queues.RANKED_2V2, new Ranked2v2());
        GameFactory.addGame(Queues.TUTORIAL, new Tutorial());

        System.out.println("[PRE-INIT] Reading config..");
        File file = new File(".env");
        GameServer.config = JConfig.newConfigObject(GameServerConfig.class);

        if (!file.exists()) {
            System.err.println("[PRE-INIT] No config found! Saving default..");
            config.save(file);
            System.err.println("[PRE-INIT] Please setup this server before running!");
            return;
        }

        config.load(file);

        if (config.matchmakingSecret().length() != 32) {
            System.err.println("[PRE-INIT] Provided secret is not 32 characters!");
            System.err.println("[PRE-INIT] Aborting..");
            System.exit(1);
            return;
        }

        System.out.println("[PRE-INIT] Starting UBot..");

        uBot = new UBot(new File(System.getProperty("user.home"), "ProjectGhost"), new UBotUpdater(), new UBotLogger());
        uBot.setVersionModule(new HttpVersionFetcher(uBot, new URL(config.getVersionURL())));

        ubotToken = uBot.startAsync();

        Scheduler.init();

        MatchFactory.setMatchCreator(new BasicMatchFactory());
        PlayerFactory.setPlayerCreator(GameServerPlayerFactory.INSTANCE);

        GameServer.server = new BaseServer(config);
        GameServer.server.getLogger().info("Connecting to matchmaking server...");
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

        server.getLogger().info("Starting heartbeat task");

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
    }

    public static void stopServer() throws IOException {
        server.getLogger().info("Disconnecting from matchmaking server...");
        matchmakingClient.disconnect();

        matchmakingClient = null;

        server.getLogger().info("Stopping server..");
        server.stop();

        ubotToken.cancel();

        server = null;

        GameFactory.shutdown();

        System.out.println("[POST-SHUTDOWN] Stopping heartbeat..");
        heartbeatTask.cancel();
        heartbeatTask = null;

        config = null;

        System.out.println("Server stopped!");
    }

    public static void restartServer() throws IOException {
        stopServer();

        ProcessBuilder builder = new ProcessBuilder("sh", "start.sh");
        builder.directory(new File(System.getProperty("user.home")));

        builder.start();
        System.exit(0);
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
