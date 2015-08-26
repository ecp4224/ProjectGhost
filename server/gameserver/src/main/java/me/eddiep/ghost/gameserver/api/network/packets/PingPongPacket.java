package me.eddiep.ghost.gameserver.api.network.packets;

import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.gameserver.api.network.TcpUdpClient;
import me.eddiep.ghost.gameserver.api.network.TcpUdpServer;

import java.io.IOException;

public class PingPongPacket extends Packet<TcpUdpServer, TcpUdpClient> {
    public PingPongPacket(TcpUdpClient client, byte[] data) {
        super(client, data);
    }



    @Override
    protected void onHandlePacket(TcpUdpClient client) throws IOException {
        int ping = consume(4).asInt();

        client.endPingTimer(ping);
        writePacket(ping);
    }

    @Override
    protected void onWritePacket(TcpUdpClient client, Object... args) throws IOException {
        client.getServer().sendUdpPacket(
                write((byte)0x09)
                .write((int)args[0])
                .endUDP()
        );
        client.startPingTimer((int)args[0]);
    }
}
