package me.eddiep.ghost.gameserver.api.network.packets;

import me.eddiep.ghost.common.game.MatchFactory;
import me.eddiep.ghost.common.game.Player;
import me.eddiep.ghost.common.network.BaseServer;
import me.eddiep.ghost.game.team.Team;
import me.eddiep.ghost.gameserver.api.GameServer;
import me.eddiep.ghost.gameserver.api.game.player.GameServerPlayerFactory;
import me.eddiep.ghost.gameserver.api.network.MatchmakingClient;
import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.network.sql.PlayerData;

import java.io.IOException;

public class CreateMatchPacket extends Packet<BaseServer, MatchmakingClient> {
    public CreateMatchPacket(MatchmakingClient client) {
        super(client);
    }

    @Override
    public void onHandlePacket(MatchmakingClient client) throws IOException {
        long mId = consume(8).asLong();
        byte team1Count = consume(1).asByte();
        byte team2Count = consume(1).asByte();

        PlayerPacketObject[] team1 = new PlayerPacketObject[team1Count];
        PlayerPacketObject[] team2 = new PlayerPacketObject[team2Count];

        for (int i = 0; i < team1.length; i++) {
            int chunkSize = consume(4).asInt();
            team1[i] = consume(chunkSize).as(PlayerPacketObject.class);
        }

        for (int i = 0; i < team2.length; i++) {
            int chunkSize = consume(4).asInt();
            team2[i] = consume(chunkSize).as(PlayerPacketObject.class);
        }

        Player[] pTeam1 = new Player[team1Count];
        Player[] pTeam2 = new Player[team2Count];

        for (int i = 0; i < team1.length; i++) {
            PlayerPacketObject p = team1[i];
            pTeam1[i] = GameServerPlayerFactory.INSTANCE.registerPlayer(p.stats.getUsername(), p.session, p.stats);
        }

        for (int i = 0; i < team2.length; i++) {
            PlayerPacketObject p = team2[i];
            pTeam2[i] = GameServerPlayerFactory.INSTANCE.registerPlayer(p.stats.getUsername(), p.session, p.stats);
        }

        Team teamOne = new Team(1, pTeam1);
        Team teamTwo = new Team(2, pTeam2);

        MatchFactory.getCreator().createMatchFor(teamOne, teamTwo, mId, GameServer.getGame().getQueue(), client.getServer());
        System.out.println("[SERVER] Created a new match for " + (pTeam1.length + pTeam2.length) + " players!");
    }

    public class PlayerPacketObject {
        private String session;
        private PlayerData stats;
    }
}
