package me.eddiep.ghost.matchmaking.network.packets;

import me.eddiep.ghost.game.match.stats.MatchHistory;
import me.eddiep.ghost.matchmaking.network.GameServerClient;
import me.eddiep.ghost.matchmaking.network.TcpServer;
import me.eddiep.ghost.matchmaking.network.database.Database;
import me.eddiep.ghost.matchmaking.player.Player;
import me.eddiep.ghost.matchmaking.player.PlayerFactory;
import me.eddiep.ghost.matchmaking.player.ranking.Rank;
import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.network.sql.PlayerData;
import me.eddiep.ghost.utils.Scheduler;

import java.io.IOException;
import java.util.HashMap;

public class MatchHistoryPacket extends Packet<TcpServer, GameServerClient> {
    public MatchHistoryPacket(GameServerClient client, byte[] data) {
        super(client, data);
    }

    @Override
    public void onHandlePacket(GameServerClient client) throws IOException {
        int packetSize = consume(4).asInt(); //Ignore this value
        int chunkSize = consume(4).asInt();
        MatchHistory match = consume(chunkSize).as(MatchHistory.class);

        /*boolean hasDodgers = consume().asBoolean();
        if (hasDodgers) {
            chunkSize = consume(4).asInt();
            PlayerPacketObject[] dodgers = consume(chunkSize).as(PlayerPacketObject[].class);

            //TODO Punish dodgers
        }*/

        if (Database.isSetup()) {
            HashMap<Long, Rank> ranks = new HashMap<>();

            for (int i = 0; i < match.team1().getTeamLength(); i++) {
                long pid = match.team1().getPlayerIds()[i];

                String member1 = match.team1().getUsernames()[i];

                Rank r;
                Player p = PlayerFactory.findPlayerByUsername(member1);
                if (p == null) {
                    r = Database.getRank(pid);
                } else {
                    r = p.getRanking();
                }

                ranks.put(pid, r);
            }

            for (int i = 0; i < match.team2().getTeamLength(); i++) {
                long pid = match.team2().getPlayerIds()[i];

                String member2 = match.team2().getUsernames()[i];

                Rank r;
                Player p = PlayerFactory.findPlayerByUsername(member2);
                if (p == null) {
                    r = Database.getRank(pid);
                } else {
                    r = p.getRanking();
                }

                ranks.put(pid, r);
            }

            //Update ranks
            if (match.winningTeam() == null || match.losingTeam() == null) {
                for (int i = 0; i < match.team1().getTeamLength(); i++) {
                    long pid = match.team1().getPlayerIds()[i];

                    Rank r = ranks.get(pid);
                    for (int z = 0; z < match.team2().getTeamLength(); z++) {
                        long ppid = match.team2().getPlayerIds()[z];

                        Rank rr = ranks.get(ppid);

                        r.addResult(pid, rr.toRankable(), 0.5);
                        rr.addResult(ppid, r.toRankable(), 0.5);
                    }
                }
            } else {
                for (int i = 0; i < match.winningTeam().getTeamLength(); i++) {
                    long winID = match.winningTeam().getPlayerIds()[i];

                    Rank r = ranks.get(winID);
                    for (int z = 0; z < match.losingTeam().getTeamLength(); z++) {
                        long loseID = match.losingTeam().getPlayerIds()[z];

                        Rank rr = ranks.get(loseID);

                        r.addResult(winID, rr.toRankable(), 1.0);
                        rr.addResult(loseID, r.toRankable(), 0.0);
                    }
                }
            }

            Database.queueTimeline(match);
            Scheduler.scheduleTask(new RankTask(ranks));
        }
    }

    private class PlayerPacketObject {
        private String session;
        private PlayerData stats;
    }

    private class RankTask implements Runnable {
        private HashMap<Long, Rank> players;
        public RankTask(HashMap<Long, Rank> players) {
            this.players = players;
        }

        @Override
        public void run() {
            for (Long id : players.keySet()) {
                Rank p = players.get(id);
                p.update();

                Database.saveRank(p);
            }
        }
    }
}
