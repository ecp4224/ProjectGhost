package me.eddiep.ghost.server.packet.impl;

import me.eddiep.ghost.server.network.Client;
import me.eddiep.ghost.server.packet.Packet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PositionPacket extends Packet {

    public PositionPacket(Client client, byte[] data) {
        super(client, data);
    }

    @Override
    public void onHandlePacket(Client client)  throws IOException {
        int packetNumber = consume(4).asInt();
        if (packetNumber < client.getLastPacketNumber()) {
            int dif = client.getLastPacketNumber() - packetNumber;
            if (dif >= Integer.MAX_VALUE - 1000) {
                client.setLastPacketNumber(packetNumber);
            } else return;
        }

        float x = consume(4).asFloat();
        float y = consume(4).asFloat();
        float xvel = consume(4).asFloat();
        float yvel = consume(4).asFloat();
    }
}
