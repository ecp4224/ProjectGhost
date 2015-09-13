package me.eddiep.ghost.matchmaking.network.packets;

import me.eddiep.ghost.game.stats.MatchHistory;
import me.eddiep.ghost.matchmaking.network.GameServerClient;
import me.eddiep.ghost.matchmaking.network.TcpServer;
import me.eddiep.ghost.matchmaking.network.database.Database;
import me.eddiep.ghost.matchmaking.player.Player;
import me.eddiep.ghost.matchmaking.player.PlayerFactory;
import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.network.sql.PlayerData;

import java.io.IOException;

public class MatchHistoryPacket extends Packet<TcpServer, GameServerClient> {
    public MatchHistoryPacket(GameServerClient client) {
        super(client);
    }

    @Override
    public void onHandlePacket(GameServerClient client) throws IOException {
        int chunkSize = consume(4).asInt();
        MatchHistory match = consume(chunkSize).as(MatchHistory.class);

        boolean hasDodgers = consume().asBoolean();
        if (hasDodgers) {
            chunkSize = consume(4).asInt();
            PlayerPacketObject[] dodgers = consume(chunkSize).as(PlayerPacketObject[].class);

            //TODO Punish dodgers
        }

        //Update ranks
        if (match.winningTeam() == null || match.losingTeam() == null) {
            for (String member1 : match.team1().getUsernames()) {
                Player p = PlayerFactory.findPlayerByUsername(member1);
                for (String member2 : match.team2().getUsernames()) {
                    Player pp = PlayerFactory.findPlayerByUsername(member2);
                    p.getRanking().addResult(pp, 0.5);
                    pp.getRanking().addResult(p, 0.5);
                }
            }
        } else {
            for (String winner : match.winningTeam().getUsernames()) {
                Player p = PlayerFactory.findPlayerByUsername(winner);
                for (String loser : match.losingTeam().getUsernames()) {
                    Player l = PlayerFactory.findPlayerByUsername(loser);
                    p.getRanking().addResult(l, 1.0);
                    l.getRanking().addResult(p, 0.0);
                }
            }
        }

        Database.queueTimeline(match);
    }

    private class PlayerPacketObject {
        private String session;
        private PlayerData stats;
    }
}
