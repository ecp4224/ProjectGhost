package me.eddiep.ghost.matchmaking.queue;

import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.matchmaking.network.gameserver.GameServer;
import me.eddiep.ghost.matchmaking.network.gameserver.GameServerFactory;
import me.eddiep.ghost.matchmaking.player.Player;
import me.eddiep.ghost.utils.ArrayHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public abstract class AbstractPlayerQueue implements PlayerQueue {
    private List<Player> playerQueue = new ArrayList<>();
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

        playerQueue.add(player);
        player.setQueue(this);

        System.out.println("[SERVER] " + player.getUsername() + " has joined the " + queue().name() + " queue!");
    }

    @Override
    public void removeUserFromQueue(Player player) {
        if (!player.isInQueue())
            return;

        playerQueue.remove(player);
        player.setQueue(null);
        System.out.println("[SERVER] " + player.getUsername() + " has left the " + queue().name() + " queue!");
    }

    @Override
    public void processQueue() {
        int max = playerQueue.size();
        if (playerQueue.size() >= 100) {
            max = max / 4;
        }

        List<Player> process = playerQueue.subList(0, max);

        playerQueue.removeAll(onProcessQueue(process));
    }

    @Override
    public QueueInfo getInfo() {
        long playersInMatch = 0;
        ArrayList<Long> matchIds = matches.get(queue());
        //TODO Get match size from game servers
        /*for (long id : matchIds) {
            Match match = MatchFactory.findMatch(id);
            playersInMatch += match.team1().getTeamLength() + match.team2().getTeamLength();
        }*/

        return new QueueInfo(queue(), playerQueue.size(), playersInMatch, description(), allyCount(), opponentCount());
    }

    protected abstract List<Player> onProcessQueue(List<Player> queueToProcess);

    public void createMatch(Player[] team1, Player[] team2) throws IOException {
        GameServer server = GameServerFactory.findLeastFullFor(queue());

        server.createMatchFor(team1, team2);

        for (Player p : ArrayHelper.combind(team1, team2)) {
            p.setQueue(null);
            p.setInMatch(true);
        }
    }
}
