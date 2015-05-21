package me.eddiep.ghost.central.network.packet.impl;

import me.eddiep.ghost.central.network.Client;
import me.eddiep.ghost.central.network.packet.Packet;

import java.io.IOException;

public class RespondRequestPacket extends Packet {
    public RespondRequestPacket(Client client) {
        super(client);
    }

    @Override
    public void onHandlePacket(Client client)  throws IOException {
        int id = consume(4).asInt();
        boolean value = consume(1).asBoolean();

        if (client != null) {
            client.respondToRequest(id, value);
        }
    }
}
