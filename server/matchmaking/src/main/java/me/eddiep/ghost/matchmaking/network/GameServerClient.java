package me.eddiep.ghost.matchmaking.network;

import me.eddiep.ghost.matchmaking.network.gameserver.GameServer;
import me.eddiep.ghost.matchmaking.network.packets.PacketFactory;
import me.eddiep.ghost.network.packet.Packet;

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

        Packet packet = PacketFactory.getGameServerPacket(opCode, this, data);
        if (packet == null)
            throw new IllegalAccessError("Invalid opcode sent!");

        packet.handlePacket();
        packet.endTCP();
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

    public synchronized void setOk(boolean ok) {
        this.isOk = ok;
        this.gotOk = true;

        this.notifyAll();
    }

    private boolean gotOk;
    private boolean isOk;
    public synchronized boolean isOk(long timeout) throws InterruptedException {
        while (true) {
            if (gotOk)
                break;

            super.wait(timeout);
        }
        gotOk = false;

        return isOk;
    }
}
