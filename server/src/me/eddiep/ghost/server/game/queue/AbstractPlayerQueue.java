package me.eddiep.ghost.server.game.queue;

import me.eddiep.ghost.server.game.Match;
import me.eddiep.ghost.server.game.impl.Player;
import me.eddiep.ghost.server.game.impl.PlayerFactory;

import java.io.IOException;
import java.util.*;

public abstract class AbstractPlayerQueue implements PlayerQueue {
    private List<UUID> playerQueue = new ArrayList<>();
    private static final HashMap<QueueType, ArrayList<Match>> matches = new HashMap<QueueType, ArrayList<Match>>();

    static {
        for (QueueType t : QueueType.values()) {
            matches.put(t, new ArrayList<Match>());
        }
    }

    @Override
    public void addUserToQueue(Player player) {
        if (player.isInQueue())
            return;

        playerQueue.add(player.getSession());
        player.setInQueue(true);
        player.setQueue(this);

        System.out.println("[SERVER] " + player.getUsername() + " has joined the " + getQueueType().name() + " queue!");
    }

    @Override
    public void removeUserFromQueue(Player player) {
        if (!player.isInQueue())
            return;

        playerQueue.remove(player.getSession());
        player.setInQueue(false);
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

    protected abstract List<UUID> onProcessQueue(List<UUID> queueToProcess);

    public abstract QueueType getQueueType();

    public void createMatch(UUID user1, UUID user2) throws IOException {
        Player player1 = PlayerFactory.findPlayerByUUID(user1);
        Player player2 = PlayerFactory.findPlayerByUUID(user2);

        Match match = new Match(player1, player2);

        match.setup();

        matches.get(getQueueType()).add(match);
    }
}
