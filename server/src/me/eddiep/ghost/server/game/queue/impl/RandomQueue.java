package me.eddiep.ghost.server.game.queue.impl;

import me.eddiep.ghost.server.Main;
import me.eddiep.ghost.server.game.queue.AbstractPlayerQueue;
import me.eddiep.ghost.server.game.queue.QueueType;
import me.eddiep.ghost.server.network.Player;
import me.eddiep.ghost.server.network.PlayerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RandomQueue extends AbstractPlayerQueue {
    @Override
    protected List<UUID> onProcessQueue(List<UUID> queueToProcess) {
        List<UUID> toRemove = new ArrayList<>();

        while (queueToProcess.size() > 1) {
            int randomIndex = Main.RANDOM.nextInt(queueToProcess.size());
            int randomIndex2;
            do {
                randomIndex2 = Main.RANDOM.nextInt(queueToProcess.size());
            } while (randomIndex2 == randomIndex);

            UUID id1 = queueToProcess.get(randomIndex);
            UUID id2 = queueToProcess.get(randomIndex);

            try {
                createMatch(id1, id2);

                queueToProcess.remove(randomIndex);
                queueToProcess.remove(randomIndex2);

                toRemove.add(id1);
                toRemove.add(id2);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return toRemove;
    }

    @Override
    public QueueType getQueueType() {
        return QueueType.Random;
    }
}
