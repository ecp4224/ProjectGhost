package me.eddiep.ghost.matchmaking.network;

import me.eddiep.ghost.matchmaking.network.gameserver.GameServer;
import me.eddiep.ghost.matchmaking.network.packets.PacketFactory;

import java.io.IOException;

public class GameServerClient extends TcpClient {
    private GameServer server;

    public GameServerClient(TcpServer server) throws IOException {
        super(server);
    }

    @Override
    public void handlePacket(byte[] rawData) throws IOException {
        byte opCode = rawData[0];
        byte[] data = new byte[rawData.length - 1];

        System.arraycopy(rawData, 1, data, 0, data.length);

        PacketFactory.getGameServerPacket(opCode, this, data).handlePacket().endTCP();
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
