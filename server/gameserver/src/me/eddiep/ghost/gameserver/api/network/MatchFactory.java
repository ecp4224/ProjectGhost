package me.eddiep.ghost.gameserver.api.network;

import me.eddiep.ghost.game.match.Match;
import me.eddiep.ghost.game.stats.MatchHistory;
import me.eddiep.ghost.game.team.Team;
import me.eddiep.ghost.gameserver.api.network.impl.BasicMatchFactory;

import java.io.IOException;
import java.util.List;

public interface MatchFactory {

    public static MatchFactory INSTANCE = new BasicMatchFactory();

    NetworkMatch createMatchFor(Team team1, Team team2, TcpUdpServer server) throws IOException;

    void endAndSaveMatch(NetworkMatch match);

    void saveMatchInfo(MatchHistory match);

    Match findMatch(long id);

    List<NetworkMatch> getAllActiveMatches();
}
