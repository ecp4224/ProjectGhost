package me.eddiep.ghost.server.game.test;

import me.eddiep.ghost.server.game.ActiveMatch;
import me.eddiep.ghost.server.game.Game;
import me.eddiep.ghost.server.game.queue.PlayerQueue;
import me.eddiep.ghost.server.game.queue.QueueInfo;

public class TestGame implements Game {
    private RandomOneVSOneQueue queue;

    @Override
    public byte id() {
        return -1;
    }

    @Override
    public byte categoryId() {
        return -1;
    }

    @Override
    public String name() {
        return "test game";
    }

    @Override
    public String description() {
        return "For debugging";
    }

    @Override
    public PlayerQueue playerQueueProcessor() {
        return queue;
    }

    @Override
    public QueueInfo queueInfo() {
        return new QueueInfo(this);
    }

    @Override
    public boolean isRanked() {
        return false;
    }

    @Override
    public void onStart() {
        queue = new RandomOneVSOneQueue(this);
    }

    @Override
    public void onQueueProcessed() {
        System.out.println("Queue processed!");
    }
}
