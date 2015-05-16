package me.eddiep.ghost.server.game.test;

import me.eddiep.ghost.server.game.Game;
import me.eddiep.ghost.server.game.queue.AbstractPlayerQueue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class RandomOneVSOneQueue extends AbstractPlayerQueue {
    private static final Random RANDOM = new Random();

    public RandomOneVSOneQueue(Game owner) {
        super(owner);
    }

    @Override
    protected List<UUID> onProcessQueue(List<UUID> queueToProcess) {
        List<UUID> toRemove = new ArrayList<>();

        while (queueToProcess.size() > 1) {
            int randomIndex = RANDOM.nextInt(queueToProcess.size());
            int randomIndex2;
            do {
                randomIndex2 = RANDOM.nextInt(queueToProcess.size());
            } while (randomIndex2 == randomIndex);

            UUID id1 = queueToProcess.get(randomIndex);
            UUID id2 = queueToProcess.get(randomIndex2);

            try {
                createMatch(id1, id2);

                int toRemove2 = randomIndex2 > randomIndex ? randomIndex2 - 1 : randomIndex2;

                queueToProcess.remove(randomIndex);
                queueToProcess.remove(toRemove2);

                toRemove.add(id1);
                toRemove.add(id2);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return toRemove;
    }

    @Override
    public int allyCount() {
        return 0;
    }

    @Override
    public int opponentCount() {
        return 1;
    }
}
