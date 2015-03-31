package me.eddiep.ghost.server.game;

import me.eddiep.ghost.server.Main;
import me.eddiep.ghost.server.game.entities.Player;
import me.eddiep.ghost.server.game.entities.Team;
import me.eddiep.ghost.server.game.queue.Queues;

import java.io.IOException;
import java.util.HashMap;

public class MatchFactory {
    private static HashMap<Long, ActiveMatch> activeMatches = new HashMap<>();

    public static Match createMatchFor(Player player1, Player player2, Queues queue) throws IOException {
        ActiveMatch match = new ActiveMatch(player1, player2);
        match.setQueueType(queue);
        match.setup();
        long id = Main.SQL.getStoredMatchCount() + activeMatches.size();
        match.setID(id);

        activeMatches.put(match.getID(), match);

        return match;
    }

    public static Match createMatchFor(Team team1, Team team2, Queues queue) throws IOException {
        ActiveMatch match = new ActiveMatch(team1, team2, team1.getTeamMembers()[0].getClient().getServer());
        match.setQueueType(queue);
        match.setup();
        long id = Main.SQL.getStoredMatchCount() + activeMatches.size();
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
        Main.SQL.saveMatch(match);
    }

    public static Match findMatch(long id) {
        if (activeMatches.containsKey(id))
            return activeMatches.get(id);
        else {
            return Main.SQL.fetchMatch(id);
        }
    }
}
