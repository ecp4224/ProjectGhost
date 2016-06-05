package com.boxtrotstudio.ghost.matchmaking.queue.impl;

import com.boxtrotstudio.ghost.matchmaking.network.gameserver.Stream;
import com.boxtrotstudio.ghost.matchmaking.player.Player;
import com.boxtrotstudio.ghost.game.queue.Queues;
import com.boxtrotstudio.ghost.matchmaking.queue.AbstractPlayerQueue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RankedQueue extends AbstractPlayerQueue {
    public RankedQueue(Stream stream) {
        super(stream);
    }

    @Override
    protected List<Player> onProcessQueue(List<Player> queueToProcess) {
        List<Player> toRemove = new ArrayList<>();

        Collections.sort(queueToProcess);

        for (int i = 0; i < queueToProcess.size(); i++) {
            Player currentPlayer = queueToProcess.get(i);

            boolean found = false;
            //Start at current index and work up the list until we find someone outside our window
            for (int z = i; z < queueToProcess.size(); z++) {
                Player playerToCompare = queueToProcess.get(z);
                if (playerToCompare == currentPlayer)
                    continue;

                if (currentPlayer.isInsideQueueWindow(playerToCompare)) {
                    if (playerToCompare.isInsideQueueWindow(currentPlayer)) {
                        try {
                            if (!createMatch(currentPlayer, playerToCompare))
                                break;

                            if (i > z) { //Remove i index first so we don't change the z index
                                queueToProcess.remove(i);
                                queueToProcess.remove(z);
                            } else {
                                queueToProcess.remove(z);
                                queueToProcess.remove(i);
                            }

                            toRemove.add(currentPlayer);
                            toRemove.add(playerToCompare);

                            found = true;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    break;
                }
            }

            if (found)
                continue;

            //Start at current index and work down the list until we find someone outside our window
            for (int z = i; z >= 0; z--) {
                Player playerToCompare = queueToProcess.get(z);
                if (playerToCompare == currentPlayer)
                    continue;

                if (currentPlayer.isInsideQueueWindow(playerToCompare)) {
                    if (playerToCompare.isInsideQueueWindow(currentPlayer)) {
                        try {
                            if (!createMatch(currentPlayer, playerToCompare))
                                continue;

                            if (i > z) { //Remove i index first so we don't change the z index
                                queueToProcess.remove(i);
                                queueToProcess.remove(z);
                            } else {
                                queueToProcess.remove(z);
                                queueToProcess.remove(i);
                            }

                            toRemove.add(currentPlayer);
                            toRemove.add(playerToCompare);
                            found = true;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    break;
                }
            }
        }

        return toRemove; //Tell manager which players were taken out of queue
    }

    @Override
    public String description() {
        return "Ranked though";
    }

    @Override
    public Queues queue() {
        return Queues.RANKED;
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
