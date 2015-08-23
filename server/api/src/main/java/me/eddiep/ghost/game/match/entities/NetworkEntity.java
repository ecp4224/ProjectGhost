package me.eddiep.ghost.game.match.entities;

import me.eddiep.ghost.network.Client;

public interface NetworkEntity extends Entity {
    boolean isConnected();

    Client getClient();
}
