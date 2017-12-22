package com.boxtrotstudio.ghost.common.game;

import com.boxtrotstudio.ghost.common.network.BaseServer;
import com.boxtrotstudio.ghost.game.match.Match;
import com.boxtrotstudio.ghost.game.queue.Queues;
import com.boxtrotstudio.ghost.game.team.Team;

import java.io.IOException;
import java.util.List;

public interface MatchCreator {
    NetworkMatch createMatchFor(Team team1, Team team2, long id, Queues queue, String mapName, BaseServer server) throws IOException;

    void endAndSaveMatch(NetworkMatch match);

    Match findMatch(long id);

    List<NetworkMatch> getAllActiveMatches();

    void createMatchFor(NetworkMatch match, long id, Queues queue, String mapName, BaseServer server);
}
