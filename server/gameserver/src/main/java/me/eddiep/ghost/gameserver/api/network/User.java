package me.eddiep.ghost.gameserver.api.network;

public interface User {
    public boolean isConnected();

    public TcpUdpClient getClient();

    public void setClient(TcpUdpClient client);
}
