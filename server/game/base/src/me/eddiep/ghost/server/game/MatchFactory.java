package me.eddiep.ghost.server.game;

import me.eddiep.ghost.server.Starter;
import me.eddiep.ghost.server.game.entities.Player;
import me.eddiep.ghost.server.game.entities.Team;
import me.eddiep.ghost.server.game.queue.QueueDescription;
import me.eddiep.ghost.server.game.stats.MatchHistory;

import java.io.IOException;
import java.util.ArrayList;

public class MatchFactory {
    private static ArrayList<ActiveMatch> activeMatches = new ArrayList<>();

    public static Match createMatchFor(Player player1, Player player2, QueueDescription queueDescription) throws IOException {
        ActiveMatch match = new ActiveMatch(player1, player2);
        match.setQueueDescription(queueDescription);
        match.setup();

        activeMatches.add(match);

        return match;
    }

    public static Match createMatchFor(Team team1, Team team2, QueueDescription queue) throws IOException {
        ActiveMatch match = new ActiveMatch(team1, team2, team1.getTeamMembers()[0].getClient().getServer());
        match.setQueueDescription(queue);
        match.setup();

        activeMatches.add(match);

        return match;
    }

    public static Match registerMatch(ActiveMatch match, Game game) throws IOException {
        match.setQueueDescription(new QueueDescription(game));
        match.setup();

        activeMatches.add(match);

        return match;
    }

    public static int getPlayerCount() {
        int count = 0;
        for (int i = 0; i < activeMatches.size(); i++) {
            count += activeMatches.get(i).getPlayerCount();
        }

        return count;
    }

    static long endAndSaveMatch(ActiveMatch match) {
        activeMatches.remove(match);

        long id = saveMatchInfo(match.matchHistory());

        match.dispose();

        return id;
    }

    private static long saveMatchInfo(MatchHistory match) {
        return Starter.getLoginBridge().saveMatchHistory(match);
    }
}
