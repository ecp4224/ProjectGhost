package me.eddiep.ghost.matchmaking.network;

import me.eddiep.ghost.matchmaking.network.gameserver.GameServer;
import me.eddiep.ghost.matchmaking.network.packets.GameServerVerificationPacket;
import me.eddiep.ghost.matchmaking.network.packets.PacketFactory;

import java.io.IOException;
import java.net.Socket;

public class GameServerClient extends TcpClient {
    private GameServer server;

    public GameServerClient(Socket socket, TcpServer server) throws IOException {
        super(socket, server);
    }

    @Override
    public void handlePacket(byte opCode) throws IOException {
        PacketFactory.getGameServerPacket(opCode, this).handlePacket().endTCP();
    }

    void validateConnection() throws IOException {
        GameServerVerificationPacket packet = new GameServerVerificationPacket(this);
        packet.handlePacket().endTCP();
    }

    public void setGameServer(GameServer server) {
        this.server = server;
    }

    public GameServer getGameServer() {
        return server;
    }

    @Override
    public void onDisconnect() throws IOException {
        super.onDisconnect();

        if (server != null) {
            server.disconnect();
        }
        server = null;
    }
}
