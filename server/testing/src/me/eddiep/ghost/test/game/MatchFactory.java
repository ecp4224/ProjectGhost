package me.eddiep.ghost.test.game;

import me.eddiep.ghost.game.match.Match;
import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.game.stats.MatchHistory;
import me.eddiep.ghost.game.team.Team;
import me.eddiep.ghost.test.network.TcpUdpServer;
import me.eddiep.ghost.test.network.world.NetworkWorld;
import me.eddiep.ghost.utils.Global;

import java.io.IOException;
import java.util.HashMap;

public class MatchFactory {
    private static HashMap<Long, NetworkMatch> activeMatches = new HashMap<>();

    public static NetworkMatch createMatchFor(Player player1, Player player2, Queues queue) throws IOException {
        NetworkMatch match = new NetworkMatch(player1, player2);
        NetworkWorld world = new NetworkWorld(match);
        match.setQueueType(queue);
        match.setWorld(world);
        match.setup();
        long id = Global.SQL.getStoredMatchCount() + activeMatches.size();
        match.setID(id);

        activeMatches.put(match.getID(), match);

        return match;
    }

    public static NetworkMatch createMatchFor(Team team1, Team team2, Queues queue, TcpUdpServer server) throws IOException {
        NetworkMatch match = new NetworkMatch(team1, team2, server);
        NetworkWorld world = new NetworkWorld(match);
        match.setQueueType(queue);
        match.setWorld(world);
        match.setup();
        long id = Global.SQL.getStoredMatchCount() + activeMatches.size();
        match.setID(id);

        activeMatches.put(match.getID(), match);

        return match;
    }

    static void endAndSaveMatch(NetworkMatch match) {
        System.out.println("[SERVER] Saving and Disposing Match: " + match.getID());
        activeMatches.remove(match.getID());

        saveMatchInfo(match.matchHistory());

        match.dispose();
    }

    private static void saveMatchInfo(MatchHistory match) {
        Global.SQL.saveMatch(match);
    }

    public static Match findMatch(long id) {
        if (activeMatches.containsKey(id))
            return activeMatches.get(id);
        else {
            return Global.SQL.fetchMatch(id);
        }
    }
}
