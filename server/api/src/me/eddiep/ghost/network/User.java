package me.eddiep.ghost.network;

import java.util.UUID;

/**
 * Represents a connected User that has a session and an attached client
 * @param <C> The type of Client this User has attached
 */
public interface User<C extends Client> {

    UUID getSession();

    C getClient();

    void setClient(C client);

    boolean isConnected();

    void respondToRequest(int id, boolean value);

    void disconnected();
}
