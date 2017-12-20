package com.boxtrotstudio.ghost.matchmaking.network;

import com.boxtrotstudio.ghost.matchmaking.core.hosts.gameserver.GameServer;
import com.boxtrotstudio.ghost.matchmaking.network.packets.PacketFactory;
import com.boxtrotstudio.ghost.network.packet.Packet;

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

        Packet<TcpServer, GameServerClient> packet = PacketFactory.getGameServerPacket(opCode);
        if (packet == null)
            throw new IllegalAccessError("Invalid opcode sent!");

        packet.handlePacket(this, data);
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
