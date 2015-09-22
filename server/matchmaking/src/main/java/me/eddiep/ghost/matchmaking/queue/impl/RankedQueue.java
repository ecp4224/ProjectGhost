package me.eddiep.ghost.matchmaking.queue.impl;

import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.matchmaking.player.Player;
import me.eddiep.ghost.matchmaking.queue.AbstractPlayerQueue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RankedQueue extends AbstractPlayerQueue {
    @Override
    protected List<Player> onProcessQueue(List<Player> queueToProcess) {

        Collections.sort(queueToProcess); //Sort current chunk so we don't have to iterate over the entire list twice
        List<Player> toRemove = new ArrayList<>();

        for (int i = 0; i < queueToProcess.size(); i++) {
            Player currentPlayer = queueToProcess.get(i);

            //Start at current index and work up the list until we find someone outside our window
            for (int z = i; z < queueToProcess.size(); z++) {
                Player playerToCompare = queueToProcess.get(z);
                if (playerToCompare == currentPlayer)
                    continue;

                if (currentPlayer.isInsideQueueWindow(playerToCompare)) {
                    if (playerToCompare.isInsideQueueWindow(currentPlayer)) {
                        try {
                            createMatch(currentPlayer, playerToCompare);

                            if (i > z) { //Remove i index first so we don't change the z index
                                queueToProcess.remove(i);
                                queueToProcess.remove(z);
                            } else {
                                queueToProcess.remove(z);
                                queueToProcess.remove(i);
                            }

                            toRemove.add(currentPlayer);
                            toRemove.add(playerToCompare);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    break; //Stop if this player is outside our window, because then all players beyond will be outside
                }
            }

            //Start at current index and work down the list until we find someone outside our window
            for (int z = i; z >= 0; z--) {
                Player playerToCompare = queueToProcess.get(z);
                if (playerToCompare == currentPlayer)
                    continue;

                if (currentPlayer.isInsideQueueWindow(playerToCompare)) {
                    if (playerToCompare.isInsideQueueWindow(currentPlayer)) {
                        try {
                            createMatch(currentPlayer, playerToCompare);

                            if (i > z) { //Remove i index first so we don't change the z index
                                queueToProcess.remove(i);
                                queueToProcess.remove(z);
                            } else {
                                queueToProcess.remove(z);
                                queueToProcess.remove(i);
                            }

                            toRemove.add(currentPlayer);
                            toRemove.add(playerToCompare);
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