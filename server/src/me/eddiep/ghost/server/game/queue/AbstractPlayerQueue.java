package me.eddiep.ghost.server.game.queue;

import me.eddiep.ghost.server.game.ActiveMatch;
import me.eddiep.ghost.server.game.Match;
import me.eddiep.ghost.server.game.MatchFactory;
import me.eddiep.ghost.server.game.team.Team;
import me.eddiep.ghost.server.game.entities.playable.impl.Player;
import me.eddiep.ghost.server.game.entities.playable.impl.PlayerFactory;
import me.eddiep.ghost.server.game.entities.playable.Playable;
import me.eddiep.ghost.server.utils.ArrayHelper;
import me.eddiep.ghost.server.utils.PFunction;

import java.io.IOException;
import java.util.*;

public abstract class AbstractPlayerQueue implements PlayerQueue {
    private List<UUID> playerQueue = new ArrayList<>();
    private static final HashMap<Queues, ArrayList<Long>> matches = new HashMap<Queues, ArrayList<Long>>();

    static {
        for (Queues t : Queues.values()) {
            matches.put(t, new ArrayList<Long>());
        }
    }

    public static List<Long> getMatchesFor(Queues type) {
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
        System.out.println("[SERVER] " + player.getUsername() + " has left the " + queue().name() + " queue!");
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
        ArrayList<Long> matchIds = matches.get(queue());
        for (long id : matchIds) {
            Match match = MatchFactory.findMatch(id);
            playersInMatch += match.team1().getTeamLength() + match.team2().getTeamLength();
        }

        return new QueueInfo(queue(), playerQueue.size(), playersInMatch, description(), allyCount(), opponentCount());
    }

    protected abstract List<UUID> onProcessQueue(List<UUID> queueToProcess);

    public void createMatch(UUID user1, UUID user2) throws IOException {
        Player player1 = PlayerFactory.findPlayerByUUID(user1);
        Player player2 = PlayerFactory.findPlayerByUUID(user2);

        ActiveMatch match = MatchFactory.createMatchFor(player1, player2, queue());

        matches.get(queue()).add(match.getID());

        onTeamEnterMatch(match.getTeam1(), match.getTeam2());
    }

    public void createMatch(Team team1, Team team2) throws IOException {
        Match match = MatchFactory.createMatchFor(team1, team2, queue());

        matches.get(queue()).add(match.getID());

        onTeamEnterMatch(team1, team2);

        ArrayHelper.assertTrueFor(team1.getTeamMembers(), new PFunction<Playable, Boolean>() {
            @Override
            public Boolean run(Playable p) {
                return p instanceof Player && ((Player)p).getQueue() == null;
            }
        }, "super.onTeamEnterMatch was not invoked!");
    }

    protected void onTeamEnterMatch(Team team1, Team team2) {
        for (Playable p : team1.getTeamMembers()) {
            if (p instanceof Player)
                ((Player)p).setQueue(null);
        }

        for (Playable p : team2.getTeamMembers()) {
            if (p instanceof Player)
                ((Player)p).setQueue(null);
        }
    }
}
