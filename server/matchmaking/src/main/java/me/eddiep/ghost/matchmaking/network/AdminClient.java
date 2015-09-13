package me.eddiep.ghost.matchmaking.network;

import java.io.IOException;

public class AdminClient extends TcpClient {
    public AdminClient(TcpServer server) throws IOException {
        super(server);
    }

    @Override
    public void handlePacket(byte[] data) throws IOException {

    }
}
