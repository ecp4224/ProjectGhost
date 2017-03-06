package com.boxtrotstudio.ghost.matchmaking.queue;

import com.amazonaws.services.gamelift.model.*;
import com.boxtrotstudio.ghost.matchmaking.Main;
import com.boxtrotstudio.ghost.matchmaking.ServerConfig;
import com.boxtrotstudio.ghost.matchmaking.network.database.Database;
import com.boxtrotstudio.ghost.matchmaking.network.gameserver.GameServer;
import com.boxtrotstudio.ghost.matchmaking.network.gameserver.GameServerFactory;
import com.boxtrotstudio.ghost.matchmaking.network.gameserver.MatchCreationExceptoin;
import com.boxtrotstudio.ghost.matchmaking.network.gameserver.Stream;
import com.boxtrotstudio.ghost.matchmaking.network.packets.MatchRedirectPacket;
import com.boxtrotstudio.ghost.matchmaking.player.Player;
import com.boxtrotstudio.ghost.network.sql.PlayerData;
import com.boxtrotstudio.ghost.utils.ArrayHelper;
import com.boxtrotstudio.ghost.utils.Global;
import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPlayerQueue implements PlayerQueue {
    private List<Player> playerQueue = new ArrayList<>();
    private final Stream stream;
    public long queueProcessStart;

    public AbstractPlayerQueue(Stream stream) {
        this.stream = stream;
    }

    @Override
    public final Stream getStream() {
        return stream;
    }

    @Override
    public void addUserToQueue(Player player) {
        if (player.isInQueue() || player.getStream() != stream)
            return;

        playerQueue.add(player);
        player.setQueue(this);

        player.getClient().getServer().getLogger().debug(player.getUsername() + " has joined the " + queue().name() + " queue!");
    }

    @Override
    public void removeUserFromQueue(Player player) {
        if (!player.isInQueue() || player.getStream() != stream)
            return;

        playerQueue.remove(player);
        player.setQueue(null);
        player.getClient().getServer().getLogger().debug(player.getUsername() + " has left the " + queue().name() + " queue!");
    }

    private int expand = 1;
    @Override
    public void processQueue() {
        int max = playerQueue.size();
        if (playerQueue.size() >= 100 && expand < 4) {
            max = max / 4;

            max *= expand;
        }

        List<Player> process = new ArrayList<>(playerQueue.subList(0, max));

        queueProcessStart = System.currentTimeMillis();
        List<Player> matches = onProcessQueue(process);

        playerQueue.removeAll(matches);

        if (matches.size() < process.size() / 2) {
            expand++;
        } else {
            expand = 1;
        }
    }

    @Override
    public long getProcessStartTime() {
        return queueProcessStart;
    }

    @Override
    public int playersInQueue() {
        return playerQueue.size();
    }

    protected abstract List<Player> onProcessQueue(List<Player> queueToProcess);

    private boolean _createGSMatch(Player[] team1, Player[] team2) throws IOException {
        GameServer server = GameServerFactory.createMatchFor(queue(), team1, team2, getStream());
        if (server == null) {
            SlackMessage message = new SlackMessage("Queue pop failed! They're no more open servers!");

            SlackAttachment attachment = new SlackAttachment();
            attachment.setText("Queue Type: " + queue().name() + "\nPlayers in Queue: " + playerQueue.size() + "\nGame Servers Online: " + GameServerFactory.getConnectedServers().size());
            attachment.setFallback("Players in Queue: " + playerQueue.size());
            message.addAttachments(attachment);

            Main.SLACK_API.call(message);
            return false;
        }

        for (Player p : ArrayHelper.combine(team1, team2)) {
            p.setQueue(null);
            p.setInMatch(true);
        }

        return true;
    }

    private boolean _createAWSMatch(Player[] team1, Player[] team2) throws IOException {
        CreateGameSessionRequest request = new CreateGameSessionRequest();

        long id = Database.getNextID();

        PlayerPacketObject[] pTeam1 = new PlayerPacketObject[team1.length];
        PlayerPacketObject[] pTeam2 = new PlayerPacketObject[team2.length];

        for (int i = 0; i < team1.length; i++) {
            Player p = team1[i];
            PlayerPacketObject obj = new PlayerPacketObject(p);
            pTeam1[i] = obj;
        }

        for (int i = 0; i < team2.length; i++) {
            Player p = team2[i];
            PlayerPacketObject obj = new PlayerPacketObject(p);
            pTeam2[i] = obj;
        }

        request
                .withGameProperties(
                    new GameProperty().withKey("queue").withValue("" + queue().asByte()),
                    new GameProperty().withKey("team1Size").withValue("" + team1.length),
                    new GameProperty().withKey("team2Size").withValue("" + team2.length),
                    new GameProperty().withKey("mID").withValue("" + id),
                    new GameProperty().withKey("team1").withValue(Global.GSON.toJson(pTeam1)),
                    new GameProperty().withKey("team2").withValue(Global.GSON.toJson(pTeam2))
                )
                .withFleetId(Main.getServer().getConfig().targetAWSFleetID())
                .withName(queue().name())
                .withMaximumPlayerSessionCount(team1.length + team2.length);

        CreateGameSessionResult result = Main.gameLiftClient.createGameSession(request);
        if (result.getGameSession() != null) {
            GameSession session = result.getGameSession();

            for (Player p : team1) {
                CreatePlayerSessionRequest playerSessionRequest = new CreatePlayerSessionRequest();
                playerSessionRequest.withGameSessionId(session.getGameSessionId())
                        .withPlayerId("" + p.getPlayerID());

                CreatePlayerSessionResult presult = Main.gameLiftClient.createPlayerSession(playerSessionRequest);

                p.setSession(p.getSession() + "@@" + presult.getPlayerSession().getPlayerSessionId());

                MatchRedirectPacket _packet = new MatchRedirectPacket(p.getClient());
                _packet.writePacket(session.getIpAddress(), session.getPort().shortValue());
            }

            for (Player p : team2) {
                CreatePlayerSessionRequest playerSessionRequest = new CreatePlayerSessionRequest();
                playerSessionRequest.withGameSessionId(session.getGameSessionId())
                        .withPlayerId("" + p.getPlayerID());

                CreatePlayerSessionResult presult = Main.gameLiftClient.createPlayerSession(playerSessionRequest);

                p.setSession(p.getSession() + "@@" + presult.getPlayerSession().getPlayerSessionId());

                MatchRedirectPacket _packet = new MatchRedirectPacket(p.getClient());
                _packet.writePacket(session.getIpAddress(), session.getPort().shortValue());
            }

            for (Player p : ArrayHelper.combine(team1, team2)) {
                p.setQueue(null);
                p.setInMatch(true);
            }

            return true;
        } else {
            return false;
        }
    }

    public boolean createMatch(Player player1, Player player2) throws IOException {
        ServerConfig config = Main.getServer().getConfig();
        if (config.useAWS()) {
            return _createAWSMatch(new Player[] { player1 }, new Player[] { player2 });
        } else {
            return _createGSMatch(new Player[] { player1 }, new Player[] { player2 });
        }
    }

    public boolean createMatch(Player[] team1, Player[] team2) throws IOException {
        ServerConfig config = Main.getServer().getConfig();
        if (config.useAWS()) {
            return _createAWSMatch(team1, team2);
        } else {
            return _createGSMatch(team1, team2);
        }
    }

    private class PlayerPacketObject {
        private String session;
        private PlayerData stats;
        private byte weapon;

        public PlayerPacketObject(Player p) {
            this.session = p.getSession();
            this.stats = p.getStats();
            this.weapon = p.getCurrentAbility().id();
        }
    }
}
