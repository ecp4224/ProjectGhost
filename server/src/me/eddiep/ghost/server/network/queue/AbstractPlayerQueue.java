package me.eddiep.ghost.server.network.queue;

import me.eddiep.ghost.server.network.Player;
import me.eddiep.ghost.server.network.PlayerFactory;

import java.util.*;

public abstract class AbstractPlayerQueue implements PlayerQueue {
    private List<UUID> playerQueue = new ArrayList<>();

    @Override
    public void addUserToQueue(Player player) {
        if (player.isInQueue())
            return;

        playerQueue.add(player.getSession());
        player.setInQueue(true);
    }

    @Override
    public void removeUserFromQueue(Player player) {
        if (!player.isInQueue())
            return;

        playerQueue.remove(player.getSession());
        player.setInQueue(false);
    }

    @Override
    public void processQueue() {
        int max = playerQueue.size();
        if (playerQueue.size() >= 100) {
            max = max / 4;
        }

        List<UUID> process = playerQueue.subList(0, max);

        onProcessQueue(process);
    }

    protected abstract void onProcessQueue(List<UUID> queueToProcess);

    public void createMatch(UUID user1, UUID user2) {
        Player player1 = PlayerFactory.findPlayerByUUID(user1);
        Player player2 = PlayerFactory.findPlayerByUUID(user2);


    }
}
