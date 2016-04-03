package com.boxtrotstudio.ghost.gameserver.api.network.packets;

import com.boxtrotstudio.ghost.common.network.packet.ChangeAbilityPacket;
import com.boxtrotstudio.ghost.game.team.Team;
import com.boxtrotstudio.ghost.gameserver.api.game.player.GameServerPlayerFactory;
import com.boxtrotstudio.ghost.gameserver.api.network.MatchmakingClient;
import com.boxtrotstudio.ghost.network.packet.Packet;
import com.boxtrotstudio.ghost.network.sql.PlayerData;
import com.boxtrotstudio.ghost.common.game.MatchFactory;
import com.boxtrotstudio.ghost.common.game.Player;
import com.boxtrotstudio.ghost.common.network.BaseServer;
import com.boxtrotstudio.ghost.game.queue.Queues;

import java.io.IOException;

public class CreateMatchPacket extends Packet<BaseServer, MatchmakingClient> {
    public CreateMatchPacket(MatchmakingClient client) {
        super(client);
    }

    @Override
    public void onHandlePacket(MatchmakingClient client) throws IOException {
        byte queueId = consume(1).asByte();
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
            pTeam1[i].setCurrentAbility(ChangeAbilityPacket.WEAPONS[p.weapon]);
        }

        for (int i = 0; i < team2.length; i++) {
            PlayerPacketObject p = team2[i];
            pTeam2[i] = GameServerPlayerFactory.INSTANCE.registerPlayer(p.stats.getUsername(), p.session, p.stats);
            pTeam2[i].setCurrentAbility(ChangeAbilityPacket.WEAPONS[p.weapon]);
        }

        Team teamOne = new Team(1, pTeam1);
        Team teamTwo = new Team(2, pTeam2);
                                                                                                    //Provided by game in factory
        MatchFactory.getCreator().createMatchFor(teamOne, teamTwo, mId, Queues.byteToType(queueId), null, client.getServer());
        System.out.println("[SERVER] Created a new match for " + (pTeam1.length + pTeam2.length) + " players!");

        MatchmakingOkPacket packet = new MatchmakingOkPacket(client);
        packet.writePacket(true);
    }

    public class PlayerPacketObject {
        private String session;
        private PlayerData stats;
        private byte weapon;
    }
}
