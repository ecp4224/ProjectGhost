package me.eddiep.ghost.server.game;

import me.eddiep.ghost.server.game.queue.PlayerQueue;
import me.eddiep.ghost.server.game.queue.QueueInfo;
import me.eddiep.ghost.server.network.dataserv.LoginServerBridge;

public interface Game {

    public byte id();

    public byte categoryId();

    public String name();

    public String description();

    public PlayerQueue playerQueueProcessor();

    public QueueInfo queueInfo();

    public boolean isRanked();

    public void onStart();

    public void onQueueProcessed();
}
