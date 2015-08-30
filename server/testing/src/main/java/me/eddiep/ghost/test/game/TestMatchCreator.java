package me.eddiep.ghost.test.game;

import me.eddiep.ghost.common.game.MatchCreator;
import me.eddiep.ghost.common.game.NetworkMatch;
import me.eddiep.ghost.common.network.BaseServer;
import me.eddiep.ghost.common.network.world.NetworkWorld;
import me.eddiep.ghost.game.match.Match;
import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.game.stats.MatchHistory;
import me.eddiep.ghost.game.team.Team;
import me.eddiep.ghost.utils.Global;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TestMatchCreator implements MatchCreator {
    private HashMap<Long, NetworkMatch> activeMatches = new HashMap<>();

    @Override
    public NetworkMatch createMatchFor(Team team1, Team team2, long id, Queues queue, BaseServer server) throws IOException {
        NetworkMatch match = new NetworkMatch(team1, team2, server);
        NetworkWorld world = new NetworkWorld(match);
        match.setQueueType(queue);
        match.setWorld(world);
        match.setup();
        match.setID(id);

        activeMatches.put(match.getID(), match);

        return match;
    }

    @Override
    public void endAndSaveMatch(NetworkMatch match) {
        //System.out.println("[SERVER] Saving and Disposing Match: " + match.getID());
        activeMatches.remove(match.getID());

        saveMatchInfo(match.matchHistory());
    }

    @Override
    public void saveMatchInfo(MatchHistory match) {
        Global.SQL.saveMatch(match);
    }

    @Override
    public Match findMatch(long id) {
        if (activeMatches.containsKey(id))
            return activeMatches.get(id);
        else {
            return Global.SQL.fetchMatch(id);
        }
    }

    @Override
    public List<NetworkMatch> getAllActiveMatches() {
        ArrayList<NetworkMatch> matches = new ArrayList<>();
        for (Long t : activeMatches.keySet()) {
            matches.add(activeMatches.get(t));
        }

        return matches;
    }

    @Override
    public void createMatchFor(NetworkMatch match, long id, Queues queue, BaseServer server) {
        NetworkWorld world = new NetworkWorld(match);
        match.setQueueType(queue);
        match.setWorld(world);
        match.setup();
        match.setID(id);

        activeMatches.put(match.getID(), match);
    }
}
