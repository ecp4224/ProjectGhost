package me.eddiep.ghost.server.game.queue;

import me.eddiep.ghost.server.game.entities.Player;

public interface PlayerQueue {

    public void addUserToQueue(Player player);

    public void removeUserFromQueue(Player player);

    public void processQueue();

    public boolean isRanked();

    public String description();

    public QueueInfo getInfo();
}
