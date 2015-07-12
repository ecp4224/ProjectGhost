package me.eddiep.ghost.test.game.queue;

import me.eddiep.ghost.game.Match;
import me.eddiep.ghost.game.entities.PlayableEntity;
import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.game.team.Team;
import me.eddiep.ghost.test.Main;
import me.eddiep.ghost.test.game.ActiveMatch;
import me.eddiep.ghost.test.game.MatchFactory;
import me.eddiep.ghost.test.game.Player;
import me.eddiep.ghost.test.game.PlayerFactory;
import me.eddiep.ghost.utils.ArrayHelper;
import me.eddiep.ghost.utils.PFunction;

import java.io.IOException;
import java.util.*;

public abstract class AbstractPlayerQueue implements PlayerQueue {
    private List<String> playerQueue = new ArrayList<>();
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

        List<String> process = playerQueue.subList(0, max);

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

    protected abstract List<String> onProcessQueue(List<String> queueToProcess);

    public void createMatch(String user1, String user2) throws IOException {
        Player player1 = PlayerFactory.findPlayerByUUID(user1);
        Player player2 = PlayerFactory.findPlayerByUUID(user2);

        ActiveMatch match = MatchFactory.createMatchFor(player1, player2, queue());

        matches.get(queue()).add(match.getID());

        onTeamEnterMatch(match.getTeam1(), match.getTeam2());
    }

    public void createMatch(Team team1, Team team2) throws IOException {
        Match match = MatchFactory.createMatchFor(team1, team2, queue(), Main.TCP_UDP_SERVER);

        matches.get(queue()).add(match.getID());

        onTeamEnterMatch(team1, team2);

        ArrayHelper.assertTrueFor(team1.getTeamMembers(), new PFunction<PlayableEntity, Boolean>() {
            @Override
            public Boolean run(PlayableEntity p) {
                return p instanceof Player && ((Player) p).getQueue() == null;
            }
        }, "super.onTeamEnterMatch was not invoked!");
    }

    protected void onTeamEnterMatch(Team team1, Team team2) {
        for (PlayableEntity p : team1.getTeamMembers()) {
            if (p instanceof Player)
                ((Player)p).setQueue(null);
        }

        for (PlayableEntity p : team2.getTeamMembers()) {
            if (p instanceof Player)
                ((Player)p).setQueue(null);
        }
    }
}
