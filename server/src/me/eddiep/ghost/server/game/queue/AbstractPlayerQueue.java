package me.eddiep.ghost.server.game.queue;

import me.eddiep.ghost.server.game.Match;
import me.eddiep.ghost.server.game.MatchFactory;
import me.eddiep.ghost.server.game.entities.Team;
import me.eddiep.ghost.server.game.entities.Player;
import me.eddiep.ghost.server.game.entities.PlayerFactory;

import java.io.IOException;
import java.util.*;

public abstract class AbstractPlayerQueue implements PlayerQueue {
    private List<UUID> playerQueue = new ArrayList<>();
    private static final HashMap<Queues, ArrayList<Integer>> matches = new HashMap<Queues, ArrayList<Integer>>();

    static {
        for (Queues t : Queues.values()) {
            matches.put(t, new ArrayList<Integer>());
        }
    }

    public static List<Integer> getMatchesFor(Queues type) {
        return Collections.unmodifiableList(matches.get(type));
    }

    @Override
    public void addUserToQueue(Player player) {
        if (player.isInQueue())
            return;

        playerQueue.add(player.getSession());
        player.setQueue(this);

        System.out.println("[SERVER] " + player.getUsername() + " has joined the " + queue().name() + " queue!");
    }

    @Override
    public void removeUserFromQueue(Player player) {
        if (!player.isInQueue())
            return;

        playerQueue.remove(player.getSession());
        player.setQueue(null);
    }

    @Override
    public void processQueue() {
        int max = playerQueue.size();
        if (playerQueue.size() >= 100) {
            max = max / 4;
        }

        List<UUID> process = playerQueue.subList(0, max);

        playerQueue.removeAll(onProcessQueue(process));
    }

    @Override
    public QueueInfo getInfo() {
        long playersInMatch = 0;
        ArrayList<Integer> matchIds = matches.get(queue());
        for (int id : matchIds) {
            Match match = MatchFactory.findMatch(id);
            playersInMatch += match.team1().getTeamLength() + match.team2().getTeamLength();
        }

        return new QueueInfo(queue(), playerQueue.size(), playersInMatch, description());
    }

    protected abstract List<UUID> onProcessQueue(List<UUID> queueToProcess);

    public void createMatch(UUID user1, UUID user2) throws IOException {
        Player player1 = PlayerFactory.findPlayerByUUID(user1);
        Player player2 = PlayerFactory.findPlayerByUUID(user2);

        Match match = MatchFactory.createMatchFor(player1, player2, queue());

        matches.get(queue()).add(match.getID());

        player1.setQueue(null);
        player2.setQueue(null);
    }

    public void createMatch(Team team1, Team team2) throws IOException {
        Match match = MatchFactory.createMatchFor(team1, team2, queue());

        matches.get(queue()).add(match.getID());

        for (Player p : team1.getTeamMembers()) {
            p.setQueue(null);
        }

        for (Player p : team2.getTeamMembers()) {
            p.setQueue(null);
        }
    }
}
