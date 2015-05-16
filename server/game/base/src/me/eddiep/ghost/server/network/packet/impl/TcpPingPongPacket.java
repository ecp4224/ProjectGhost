package me.eddiep.ghost.server.network.packet.impl;

import me.eddiep.ghost.server.network.Client;
import me.eddiep.ghost.server.network.packet.Packet;

import java.io.IOException;

public class TcpPingPongPacket extends Packet {
    public TcpPingPongPacket(Client client) {
        super(client);
    }

    @Override
    protected void onHandlePacket(Client client) throws IOException {
        int ping = consume(4).asInt();

        client.endPingTimer(ping);
        writePacket(ping);
    }

    @Override
    protected void onWritePacket(Client client, Object... args) throws IOException {
        write((byte)0x19)
        .write((int)args[0])
        .endTCP();
    }
}
