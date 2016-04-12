package me.eddiep.ghost.ai;

import com.boxtrotstudio.ghost.common.game.MatchCreator;
import com.boxtrotstudio.ghost.common.game.NetworkMatch;
import com.boxtrotstudio.ghost.common.network.BaseServer;
import com.boxtrotstudio.ghost.common.network.world.NetworkWorld;
import com.boxtrotstudio.ghost.game.match.Match;
import com.boxtrotstudio.ghost.game.match.stats.MatchHistory;
import com.boxtrotstudio.ghost.game.queue.Queues;
import com.boxtrotstudio.ghost.game.team.Team;
import com.boxtrotstudio.ghost.utils.Global;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BotMatchCreator implements MatchCreator {
    HashMap<Long, NetworkMatch> matches = new HashMap<>();

    @Override
    public NetworkMatch createMatchFor(Team team1, Team team2, long id, Queues queue, String mapName, BaseServer server) throws IOException {
        NetworkMatch match = new BotMatch(team1, team2, server);
        NetworkWorld world = new NetworkWorld("test", match);
        match.setQueueType(queue);
        match.setWorld(world);
        match.setup();
        match.setID(id);

        matches.put(id, match);

        return match;
    }

    @Override
    public void endAndSaveMatch(NetworkMatch match) {
        if (match.disconnectdPlayers.size() > 0) { //Players disconnected during this match!
            System.out.println(match.disconnectdPlayers.size() + " players disconnected from this match!");
        }
        matches.remove(match.getID());

        saveMatchInfo(match.matchHistory());
    }

    public void saveMatchInfo(MatchHistory match) {
        Global.SQL.saveMatch(match);
    }

    @Override
    public Match findMatch(long id) {
        return matches.get(id);
    }

    @Override
    public List<NetworkMatch> getAllActiveMatches() {
        return new ArrayList<>(matches.values());
    }

    @Override
    public void createMatchFor(NetworkMatch match, long id, Queues queue, String mapName, BaseServer server) {
        NetworkWorld world = new NetworkWorld("test", match);
        match.setQueueType(queue);
        match.setWorld(world);
        match.setup();
        match.setID(id);

        matches.put(id, match);
    }
}
