package me.eddiep.ghost.server.game.entities;

import me.eddiep.ghost.server.game.Entity;
import me.eddiep.ghost.server.network.Client;

public interface NetworkEntity extends Entity {
    boolean isConnected();

    Client getClient();
}
