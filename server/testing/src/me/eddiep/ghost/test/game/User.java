package me.eddiep.ghost.test.game;

import me.eddiep.ghost.test.network.TcpUdpClient;

public interface User {
    boolean isConnected();

    TcpUdpClient getClient();

    void setClient(TcpUdpClient client);
}
