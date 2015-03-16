package me.eddiep.ghost.server.game.queue.impl;

import me.eddiep.ghost.server.Main;
import me.eddiep.ghost.server.game.entities.Team;
import me.eddiep.ghost.server.game.queue.AbstractPlayerQueue;
import me.eddiep.ghost.server.game.queue.QueueType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RandomTwoVSTwoQueue extends AbstractPlayerQueue {
    @Override
    protected List<UUID> onProcessQueue(List<UUID> queueToProcess) {
        List<UUID> toRemove = new ArrayList<>();

        while (queueToProcess.size() > 3) {
            int player1 = getRandomIndex(queueToProcess.size());
            int player2 = getRandomIndex(queueToProcess.size(), player1);
            int player3 = getRandomIndex(queueToProcess.size(), player1, player2);
            int player4 = getRandomIndex(queueToProcess.size(), player1, player2, player3);

            UUID id1 = queueToProcess.get(player1);
            UUID id2 = queueToProcess.get(player2);
            UUID id3 = queueToProcess.get(player3);
            UUID id4 = queueToProcess.get(player4);

            try {
                createMatch(new Team(1, id1, id3), new Team(2, id2, id4));

                queueToProcess.remove(id1);
                queueToProcess.remove(id2);
                queueToProcess.remove(id3);
                queueToProcess.remove(id4);

                toRemove.add(id1);
                toRemove.add(id2);
                toRemove.add(id3);
                toRemove.add(id4);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return toRemove;
    }

    private int getRandomIndex(int max, int... toExclude) {
        List<Integer> exclude = new ArrayList<>();
        for (int i : toExclude) {
            exclude.add(i);
        }

        int toReturn;
        do {
            toReturn = Main.RANDOM.nextInt(max);
        } while (exclude.contains(toReturn));

        return toReturn;
    }

    @Override
    public QueueType getQueueType() {
        return QueueType.Random2V2;
    }
}
