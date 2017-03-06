package com.boxtrotstudio.ghost.gameserver.api;

import com.boxtrotstudio.aws.GameLiftServerAPI;
import com.boxtrotstudio.aws.ProcessParameters;
import com.boxtrotstudio.aws.common.GenericOutcome;
import com.boxtrotstudio.aws.model.GameSession;
import com.boxtrotstudio.ghost.common.game.MatchFactory;
import com.boxtrotstudio.ghost.common.game.NetworkMatch;
import com.boxtrotstudio.ghost.common.game.PlayerFactory;
import com.boxtrotstudio.ghost.common.game.gamemodes.tutorial.TutorialBot;
import com.boxtrotstudio.ghost.common.game.gamemodes.tutorial.TutorialMatch;
import com.boxtrotstudio.ghost.common.network.BaseServer;
import com.boxtrotstudio.ghost.common.network.packet.ChangeAbilityPacket;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.queue.Queues;
import com.boxtrotstudio.ghost.game.team.Team;
import com.boxtrotstudio.ghost.gameserver.api.game.player.GameServerPlayerFactory;
import com.boxtrotstudio.ghost.gameserver.api.network.MatchmakingClient;
import com.boxtrotstudio.ghost.gameserver.api.network.impl.BasicMatchFactory;
import com.boxtrotstudio.ghost.gameserver.api.network.packets.CreateMatchPacket;
import com.boxtrotstudio.ghost.gameserver.api.network.packets.GameServerHeartbeat;
import com.boxtrotstudio.ghost.gameserver.common.*;
import com.boxtrotstudio.ghost.network.sql.PlayerData;
import com.boxtrotstudio.ghost.utils.CancelToken;
import com.boxtrotstudio.ghost.utils.Global;
import com.boxtrotstudio.ghost.utils.Scheduler;
import com.boxtrotstudio.ghost.utils.WebUtils;
import me.eddiep.jconfig.JConfig;
import me.eddiep.ubot.UBot;
import me.eddiep.ubot.module.impl.HttpVersionFetcher;
import me.eddiep.ubot.module.impl.Log4JModule;
import org.apache.logging.log4j.Logger;

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
        System.out.println("[PRE-INIT] Initialize AWS...");
        GenericOutcome result = GameLiftServerAPI.initSdk();

        if (!result.isSuccessful())
            throw result.getError();

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

        Scheduler.init();

        MatchFactory.setMatchCreator(new BasicMatchFactory());
        PlayerFactory.setPlayerCreator(GameServerPlayerFactory.INSTANCE);

        GameServer.server = new BaseServer(config);

        GameServer.server.getLogger().info("Starting UBot...");
        Log4JModule logger = new Log4JModule(GameServer.server.getLogger());
        File directory = new File(System.getProperty("user.home"), "ProjectGhost");
        UBotScheduler scheduler = new UBotScheduler();

        uBot = new UBot(directory, scheduler, logger, logger);

        HttpVersionFetcher fetcher = new HttpVersionFetcher(uBot, new URL(config.getVersionURL()), new File(config.getVersionFile()));
        uBot.setUpdateModule(fetcher);

        ubotToken = uBot.startAsync();

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

        server.getLogger().info("Notifying AWS");
        File[] files = new File[] {
            new File("all.log")
        };

        ProcessParameters parameters = new ProcessParameters(server.getLocalPort(), files);
        parameters.whenGameSessionStarts(GameServer::gameServerStarted);
        parameters.whenProcessTerminate(GameServer::shutdown);
        parameters.whenHealthCheck(() -> true);

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

    public static void gameServerStarted(GameSession session) {
        byte queueId = Byte.parseByte(session.getGameProperty("queue"));
        int team1Szie = Integer.parseInt(session.getGameProperty("team1Size"));
        int team2Size = Integer.parseInt(session.getGameProperty("team2Size"));
        long mId = Long.parseLong(session.getGameProperty("mID"));

        String team1Json = session.getGameProperty("team1");
        String team2Json = session.getGameProperty("team2");

        PlayerPacketObject[] team1 = Global.GSON.fromJson(team1Json, PlayerPacketObject[].class);
        PlayerPacketObject[] team2 = Global.GSON.fromJson(team2Json, PlayerPacketObject[].class);

        PlayableEntity[] pTeam1 = new PlayableEntity[team1Szie];
        PlayableEntity[] pTeam2 = new PlayableEntity[team2Size];

        for (int i = 0; i < team1.length; i++) {
            PlayerPacketObject p = team1[i];
            pTeam1[i] = GameServerPlayerFactory.INSTANCE.registerPlayer(p.stats.getUsername(), p.session, p.stats);
            pTeam1[i]._packet_setCurrentAbility(ChangeAbilityPacket.WEAPONS[p.weapon]);
        }

        for (int i = 0; i < team2.length; i++) {
            PlayerPacketObject p = team2[i];
            pTeam2[i] = GameServerPlayerFactory.INSTANCE.registerPlayer(p.stats.getUsername(), p.session, p.stats);
            pTeam2[i]._packet_setCurrentAbility(ChangeAbilityPacket.WEAPONS[p.weapon]);
        }

        if (Queues.byteToType(queueId) == Queues.TUTORIAL) { //This is a tutorial match
            Team teamOne = new Team(1, pTeam1);
            Team botTeam = new Team(2, new TutorialBot());

            TutorialMatch tutorialMatch = new TutorialMatch(teamOne, botTeam, server);
            MatchFactory.getCreator().createMatchFor(tutorialMatch, mId, Queues.byteToType(queueId), null, server);
        } else {
            Team teamOne = new Team(1, pTeam1);
            Team teamTwo = new Team(2, pTeam2);
            //Provided by game in factory
            try {
                MatchFactory.getCreator().createMatchFor(teamOne, teamTwo, mId, Queues.byteToType(queueId), null, server);
                server.getLogger().debug("Created a new match for " + (pTeam1.length + pTeam2.length) + " players!");
            } catch (IOException e) {
                server.getLogger().error("Failed to create a new match", e);
            }
        }
    }

    public static Logger getLogger() {
        return GameServer.server.getLogger();
    }

    public static void shutdown() {
        try {
            stopServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        GameLiftServerAPI.destroy();
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

        System.out.println("[POST-SHUTDOWN] Notifying AWS..");
        GameLiftServerAPI.processEnding();

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

    public class PlayerPacketObject {
        private String session;
        private PlayerData stats;
        private byte weapon;
    }
}
