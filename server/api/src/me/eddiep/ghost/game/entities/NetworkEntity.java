package me.eddiep.ghost.game.entities;

import me.eddiep.ghost.network.Client;
import me.eddiep.ghost.game.Entity;

public interface NetworkEntity extends Entity {
    boolean isConnected();

    Client getClient();
}
