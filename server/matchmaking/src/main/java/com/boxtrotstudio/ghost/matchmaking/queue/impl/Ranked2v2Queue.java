package com.boxtrotstudio.ghost.matchmaking.queue.impl;

import com.boxtrotstudio.ghost.game.queue.Queues;
import com.boxtrotstudio.ghost.matchmaking.network.gameserver.Stream;
import com.boxtrotstudio.ghost.matchmaking.player.Player;
import com.boxtrotstudio.ghost.matchmaking.queue.AbstractPlayerQueue;
import com.boxtrotstudio.ghost.utils.Global;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Ranked2v2Queue extends AbstractPlayerQueue {
    public Ranked2v2Queue(Stream stream) {
        super(stream);
    }

    @Override
    protected List<Player> onProcessQueue(List<Player> queueToProcess) {
        List<Player> toRemove = new ArrayList<>();

        while (queueToProcess.size() > 3) {
            int player1 = getRandomIndex(queueToProcess.size());
            int player2 = getRandomIndex(queueToProcess.size(), player1);
            int player3 = getRandomIndex(queueToProcess.size(), player1, player2);
            int player4 = getRandomIndex(queueToProcess.size(), player1, player2, player3);

            Player p1 = queueToProcess.get(player1);
            Player p2 = queueToProcess.get(player2);
            Player p3 = queueToProcess.get(player3);
            Player p4 = queueToProcess.get(player4);

            try {
                createMatch(new Player[] { p1, p3 }, new Player[] { p2, p4 });

                queueToProcess.remove(p1);
                queueToProcess.remove(p2);
                queueToProcess.remove(p3);
                queueToProcess.remove(p4);

                toRemove.add(p1);
                toRemove.add(p2);
                toRemove.add(p3);
                toRemove.add(p4);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return toRemove;
    }

    private int getRandomIndex(int max, int... toExclude) {
        List<Integer> exclude = new ArrayList<>();
        for (int i : toExclude) { //Because java is stupid
            exclude.add(i);
        }

        int toReturn;
        do {
            toReturn = Global.RANDOM.nextInt(max);
        } while (exclude.contains(toReturn));

        return toReturn;
    }

    @Override
    public Queues queue() {
        return Queues.RANKED_2V2;
    }

    @Override
    public int allyCount() {
        return 1;
    }

    @Override
    public int opponentCount() {
        return 2;
    }

    @Override
    public String description() {
        return "Face a random opponent in a 1v1 match to the death. [3 Lives]";
    }
}
