package me.eddiep.ghost.matchmaking.queue.impl;

import me.eddiep.ghost.game.queue.Queues;

public class OriginalQueue extends DemoQueue {
    @Override
    public Queues queue() {
        return Queues.ORIGINAL;
    }

    @Override
    public int allyCount() {
        return 0;
    }

    @Override
    public int opponentCount() {
        return 1;
    }

    @Override
    public String description() {
        return "Face a random opponent in a 1v1 match to the death. [3 Lives]";
    }
}
