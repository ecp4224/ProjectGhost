package com.boxtrotstudio.ghost.matchmaking.core.hosts;

import com.amazonaws.services.gamelift.model.*;
import com.boxtrotstudio.ghost.game.queue.Queues;
import com.boxtrotstudio.ghost.matchmaking.Main;
import com.boxtrotstudio.ghost.matchmaking.core.MatchHost;
import com.boxtrotstudio.ghost.matchmaking.core.hosts.gameserver.Stream;
import com.boxtrotstudio.ghost.matchmaking.network.database.Database;
import com.boxtrotstudio.ghost.matchmaking.network.packets.MatchRedirectPacket;
import com.boxtrotstudio.ghost.matchmaking.player.Player;
import com.boxtrotstudio.ghost.utils.Global;

import java.io.IOException;

public class AWS implements MatchHost {
    @Override
    public boolean createMatch(Player[] team1, Player[] team2, Queues queue, Stream stream) throws IOException {
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
                        new GameProperty().withKey("queue").withValue("" + queue.asByte()),
                        new GameProperty().withKey("team1Size").withValue("" + team1.length),
                        new GameProperty().withKey("team2Size").withValue("" + team2.length),
                        new GameProperty().withKey("mID").withValue("" + id),
                        new GameProperty().withKey("team1").withValue(Global.GSON.toJson(pTeam1)),
                        new GameProperty().withKey("team2").withValue(Global.GSON.toJson(pTeam2))
                )
                .withFleetId(Main.getServer().getConfig().targetAWSFleetID())
                .withName(queue.name())
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

            return true;
        } else {
            return false;
        }
    }

    @Override
    public int size() {
        return 0; //TODO Implement this?
    }

    @Override
    public void scaleUp() { }

    @Override
    public void scaleDown() { }


    private class PlayerPacketObject {
        private String username;
        private byte weapon;

        public PlayerPacketObject(Player p) {
            this.username = p.getUsername();
            this.weapon = p.getCurrentAbility().id();
        }
    }
}
