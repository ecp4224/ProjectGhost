package me.eddiep.ghost.common.game;


import me.eddiep.ghost.common.network.BasePlayerClient;

public interface User {
    boolean isConnected();

    BasePlayerClient getClient();

    void setClient(BasePlayerClient client);
}
