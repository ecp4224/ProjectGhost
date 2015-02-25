package me.eddiep.ghost.server.game.queue.impl;

import me.eddiep.ghost.server.game.queue.AbstractPlayerQueue;
import me.eddiep.ghost.server.game.queue.QueueType;

import java.util.List;
import java.util.UUID;

public class RandomQueue extends AbstractPlayerQueue {
    @Override
    protected void onProcessQueue(List<UUID> queueToProcess) {

    }

    @Override
    public QueueType getQueueType() {
        return QueueType.Random;
    }
}
