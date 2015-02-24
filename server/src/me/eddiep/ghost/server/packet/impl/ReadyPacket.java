package me.eddiep.ghost.server.packet.impl;

import me.eddiep.ghost.server.network.Client;
import me.eddiep.ghost.server.packet.Packet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ReadyPacket extends Packet {

    public ReadyPacket(Client client) {
        super(client);
    }

    @Override
    public void onHandlePacket(Client client) throws IOException {
        client.getPlayer().setReady(consume().asBoolean());
    }
}
