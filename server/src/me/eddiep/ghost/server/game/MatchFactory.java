package me.eddiep.ghost.server.game;

import me.eddiep.ghost.server.TcpUdpServer;
import me.eddiep.ghost.server.game.entities.Player;
import me.eddiep.ghost.server.game.entities.Team;

import java.io.IOException;
import java.util.HashMap;

public class MatchFactory {
    private static int lastID = 0;
    private static HashMap<Integer, ActiveMatch> activeMatches = new HashMap<>();

    public static Match createMatchFor(Player player1, Player player2) throws IOException {
        ActiveMatch match = new ActiveMatch(player1, player2);

        match.setup();
        lastID++;
        match.setID(lastID);

        activeMatches.put(match.getID(), match);

        return match;
    }

    public static Match createMatchFor(Team team1, Team team2) throws IOException {
        ActiveMatch match = new ActiveMatch(team1, team2, team1.getTeamMembers()[0].getClient().getServer());

        match.setup();
        lastID++;
        match.setID(lastID);

        activeMatches.put(match.getID(), match);

        return match;
    }

    public static Match findMatch(int id) {
        if (activeMatches.containsKey(id))
            return activeMatches.get(id);
        else {
            //TODO Find match in database
            return null;
        }
    }
}
