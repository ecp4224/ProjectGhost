package me.eddiep.ghost.server.network.packet.impl;

import me.eddiep.ghost.server.network.Client;
import me.eddiep.ghost.server.network.dataserv.CentralServer;
import me.eddiep.ghost.server.network.packet.Packet;

import java.io.IOException;

public class CentralServerPacket extends Packet {
    public CentralServerPacket(Client client) {
        super(client);
    }

    @Override
    public void onHandlePacket(Client client) throws IOException {
        if (!(client instanceof CentralServer)) {
            throw new SecurityException("Arbitrary client made an attempt to send a CentralServer Packet !");
        }
    }

    @Override
    public void onWritePacket(Client client, Object... args) throws IOException {
        if (!(client instanceof CentralServer)) {
            throw new SecurityException("An attempt was made to send a CentralServer Packet to an arbitrary client !");
        }
    }
}
