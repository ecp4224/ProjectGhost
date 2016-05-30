package com.boxtrotstudio.ghost.matchmaking.queue.impl;

import com.boxtrotstudio.ghost.matchmaking.network.gameserver.Stream;
import com.boxtrotstudio.ghost.game.queue.Queues;

public class OriginalQueue extends DemoQueue {
    public OriginalQueue(Stream stream) {
        super(stream);
    }

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
