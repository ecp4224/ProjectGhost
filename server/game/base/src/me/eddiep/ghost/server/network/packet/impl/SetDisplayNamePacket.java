package me.eddiep.ghost.server.network.packet.impl;

import me.eddiep.ghost.server.Starter;
import me.eddiep.ghost.server.network.Client;
import me.eddiep.ghost.server.network.packet.Packet;

import java.io.IOException;

public class SetDisplayNamePacket extends Packet {
    public SetDisplayNamePacket(Client client) {
        super(client);
    }

    @Override
    public void onHandlePacket(Client client)  throws IOException {
        byte length = consume(1).asByte();
        String displayName = consume(length).asString();

        displayName = displayName.replaceAll("[^A-Za-z0-9 ]", "");

        if (client.getPlayer().getDisplayName().equals(displayName)) {
            OkPacket packet = new OkPacket(client);
            packet.writePacket(true);
        } else if (Starter.getLoginBridge().doesDisplayNameExists(displayName)) {
            OkPacket packet = new OkPacket(client);
            packet.writePacket(false);
        } else {
            client.getPlayer().setDisplayName(displayName);
            OkPacket packet = new OkPacket(client);
            packet.writePacket(true);
        }
    }
}
