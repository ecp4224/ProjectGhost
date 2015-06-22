package me.eddiep.ghost.network;

import java.util.UUID;

public interface User<C extends Client> {

    UUID getSession();

    C getClient();

    void setClient(C client);

    boolean isConnected();

    void respondToRequest(int id, boolean value);

    void disconnected();
}
