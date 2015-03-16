package me.eddiep.ghost.server.network.packet.impl;

import me.eddiep.ghost.server.network.Client;
import me.eddiep.ghost.server.network.packet.Packet;

import java.io.IOException;

public class TCPPingPacket extends Packet {
    public TCPPingPacket(Client client) {
        super(client);
    }

    @Override
    protected void onHandlePacket(Client client) throws IOException {
        byte val = consume(1).asByte();
    }
}
