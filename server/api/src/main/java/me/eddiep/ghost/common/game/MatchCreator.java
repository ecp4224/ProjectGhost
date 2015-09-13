package me.eddiep.ghost.common.game;

import me.eddiep.ghost.common.network.BaseServer;
import me.eddiep.ghost.game.match.Match;
import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.game.team.Team;

import java.io.IOException;
import java.util.List;

public interface MatchCreator {
    NetworkMatch createMatchFor(Team team1, Team team2, long id, Queues queue, String mapName, BaseServer server) throws IOException;

    void endAndSaveMatch(NetworkMatch match);

    Match findMatch(long id);

    List<NetworkMatch> getAllActiveMatches();

    void createMatchFor(NetworkMatch match, long id, Queues queue, String mapName, BaseServer server);
}
