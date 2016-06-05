package com.boxtrotstudio.ghost.game.match.entities;

import com.boxtrotstudio.ghost.network.Client;

public interface NetworkEntity extends Entity {
    boolean isConnected();

    Client getClient();
}
