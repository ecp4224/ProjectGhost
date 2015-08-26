package me.eddiep.ghost.gameserver.api.network.packets;

import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.gameserver.api.network.TcpUdpClient;
import me.eddiep.ghost.gameserver.api.network.TcpUdpServer;

import java.io.IOException;

public class TcpPingPongPacket extends Packet<TcpUdpServer, TcpUdpClient> {
    public TcpPingPongPacket(TcpUdpClient client) {
        super(client);
    }

    @Override
    protected void onHandlePacket(TcpUdpClient client) throws IOException {
        int ping = consume(4).asInt();

        client.endPingTimer(ping);
        writePacket(ping);
    }

    @Override
    protected void onWritePacket(TcpUdpClient client, Object... args) throws IOException {
        write((byte)0x19)
        .write((int)args[0])
        .endTCP();
    }
}
