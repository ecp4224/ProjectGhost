package me.eddiep.ghost.matchmaking.network.packets;

import me.eddiep.ghost.game.stats.MatchHistory;
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

public class MatchHistoryPacket extends Packet<TcpServer, GameServerClient> {
    public MatchHistoryPacket(GameServerClient client, byte[] data) {
        super(client, data);
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

        if (Database.isSetup()) {
            Rank[] ranks = new Rank[match.team1().getTeamLength() + match.team2().getTeamLength()];


            //Update ranks
            if (match.winningTeam() == null || match.losingTeam() == null) {
                for (int i = 0; i < match.team1().getTeamLength(); i++) {
                    String member1 = match.team1().getUsernames()[i];
                    long pid = match.team1().getPlayerIds()[i];

                    Rank r;
                    Player p = PlayerFactory.findPlayerByUsername(member1);
                    if (p == null) {
                        r = Database.getRank(pid);
                    } else {
                        r = p.getRanking();
                    }
                    ranks[i] = r;
                    for (int z = 0; z < match.team2().getTeamLength(); z++) {
                        String member2 = match.team2().getUsernames()[z];
                        long ppid = match.team2().getPlayerIds()[z];

                        Player pp = PlayerFactory.findPlayerByUsername(member2);
                        Rank rr;
                        if (pp == null) {
                            rr = Database.getRank(ppid);
                        } else {
                            rr = pp.getRanking();
                        }
                        ranks[z + match.team1().getTeamLength()] = rr;

                        r.addResult(pid, rr.toRankable(), 0.5);
                        rr.addResult(ppid, r.toRankable(), 0.5);
                    }
                }
            } else {
                for (int i = 0; i < match.winningTeam().getTeamLength(); i++) {
                    String winner = match.winningTeam().getUsernames()[i];
                    long winID = match.winningTeam().getPlayerIds()[i];

                    Player p = PlayerFactory.findPlayerByUsername(winner);
                    Rank r;
                    if (p == null) {
                        r = Database.getRank(winID);
                    } else {
                        r = p.getRanking();
                    }
                    ranks[i] = r;

                    for (int z = 0; z < match.losingTeam().getTeamLength(); z++) {
                        String loser = match.losingTeam().getUsernames()[z];
                        long loseID = match.losingTeam().getPlayerIds()[z];

                        Player l = PlayerFactory.findPlayerByUsername(loser);
                        Rank rr;
                        if (l == null) {
                            rr = Database.getRank(loseID);
                        } else {
                            rr = l.getRanking();
                        }
                        ranks[z + match.winningTeam().getTeamLength()] = rr;

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
        private Rank[] players;
        public RankTask(Rank... players) {
            this.players = players;
        }

        @Override
        public void run() {
            for (Rank p : players) {
                p.update();
            }
        }
    }
}
