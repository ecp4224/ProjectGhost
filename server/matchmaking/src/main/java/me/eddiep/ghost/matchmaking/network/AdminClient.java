package me.eddiep.ghost.matchmaking.network;

import java.io.IOException;
import java.net.Socket;

public class AdminClient extends TcpClient {
    public AdminClient(Socket socket, TcpServer server) throws IOException {
        super(socket, server);
    }

    public void verifyConnection() {

    }

    @Override
    public void handlePacket(byte opCode) throws IOException {

    }
}
