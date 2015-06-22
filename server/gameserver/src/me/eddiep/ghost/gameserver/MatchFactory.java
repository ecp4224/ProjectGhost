package me.eddiep.ghost.gameserver;

import me.eddiep.ghost.matchmaking.TcpServer;
import me.eddiep.ghost.utils.Global;
import me.eddiep.ghost.game.Match;
import me.eddiep.ghost.game.entities.playable.impl.BaseNetworkPlayer;
import me.eddiep.ghost.matchmaking.queue.Queues;
import me.eddiep.ghost.game.stats.MatchHistory;
import me.eddiep.ghost.game.team.Team;

import java.io.IOException;
import java.util.HashMap;

public class MatchFactory {
    private static HashMap<Long, ActiveMatch> activeMatches = new HashMap<>();

    public static ActiveMatch createMatchFor(BaseNetworkPlayer player1, BaseNetworkPlayer player2, Queues queue) throws IOException {
        ActiveMatch match = new ActiveMatch(player1, player2);
        match.setQueueType(queue);
        match.setup();
        long id = Global.SQL.getStoredMatchCount() + activeMatches.size();
        match.setID(id);

        activeMatches.put(match.getID(), match);

        return match;
    }

    public static ActiveMatch createMatchFor(Team team1, Team team2, Queues queue, TcpServer server) throws IOException {
        ActiveMatch match = new ActiveMatch(team1, team2, server);
        match.setQueueType(queue);
        match.setup();
        long id = Global.SQL.getStoredMatchCount() + activeMatches.size();
        match.setID(id);

        activeMatches.put(match.getID(), match);

        return match;
    }

    static void endAndSaveMatch(ActiveMatch match) {
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
