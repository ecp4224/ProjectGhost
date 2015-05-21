package me.eddiep.ghost.central.network.packet.impl;

import me.eddiep.ghost.central.network.Client;
import me.eddiep.ghost.central.network.packet.Packet;

import java.io.IOException;

public class PingPongPacket extends Packet {
    public PingPongPacket(Client client, byte[] data) {
        super(client, data);
    }



    @Override
    protected void onHandlePacket(Client client) throws IOException {
        int ping = consume(4).asInt();

        client.endPingTimer(ping);
        writePacket(ping);
    }

    @Override
    protected void onWritePacket(Client client, Object... args) throws IOException {
        client.getServer().sendUdpPacket(
                write((byte)0x09)
                .write((int)args[0])
                .endUDP()
        );
        client.startPingTimer((int)args[0]);
    }
}
