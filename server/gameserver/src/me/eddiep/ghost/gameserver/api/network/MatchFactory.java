package me.eddiep.ghost.gameserver.api.network;

import me.eddiep.ghost.game.Match;
import me.eddiep.ghost.game.entities.playable.impl.BaseNetworkPlayer;
import me.eddiep.ghost.game.stats.MatchHistory;
import me.eddiep.ghost.game.team.Team;
import me.eddiep.ghost.gameserver.api.network.impl.BasicMatchFactory;

import java.io.IOException;
import java.util.List;

public interface MatchFactory {

    public static MatchFactory INSTANCE = new BasicMatchFactory();

    ActiveMatch createMatchFor(BaseNetworkPlayer player1, BaseNetworkPlayer player2) throws IOException;

    ActiveMatch createMatchFor(Team team1, Team team2, TcpUdpServer server) throws IOException;

    void endAndSaveMatch(ActiveMatch match);

    void saveMatchInfo(MatchHistory match);

    Match findMatch(long id);

    List<ActiveMatch> getAllActiveMatches();
}
