package com.boxtrotstudio.ghost.common.game;


import com.boxtrotstudio.ghost.common.network.BasePlayerClient;

public interface User {
    boolean isConnected();

    BasePlayerClient getClient();

    void setClient(BasePlayerClient client);
}
