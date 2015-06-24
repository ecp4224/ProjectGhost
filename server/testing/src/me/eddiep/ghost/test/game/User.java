package me.eddiep.ghost.test.game;

import me.eddiep.ghost.test.network.TcpUdpClient;

public interface User {
    public boolean isConnected();

    public TcpUdpClient getClient();

    public void setClient(TcpUdpClient client);


}
