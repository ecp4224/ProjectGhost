package me.eddiep.ghost.matchmaking.network;

import me.eddiep.ghost.matchmaking.network.packets.AdminVerifyPacket;

import java.io.IOException;
import java.net.Socket;

public class AdminClient extends TcpClient {
    public AdminClient(Socket socket, TcpServer server) throws IOException {
        super(socket, server);
    }

    public void verifyConnection() throws IOException {
        AdminVerifyPacket packet = new AdminVerifyPacket(this);
        packet.handlePacket().endTCP();
    }

    @Override
    public void handlePacket(byte opCode) throws IOException {

    }
}
