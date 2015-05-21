package me.eddiep.ghost.central.network.packet.impl;

import me.eddiep.ghost.central.Main;
import me.eddiep.ghost.central.network.Client;
import me.eddiep.ghost.central.network.packet.Packet;

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

        if (client.getDisplayName().equals(displayName)) {
            OkPacket packet = new OkPacket(client);
            packet.writePacket(true);
        } else if (Main.getLoginBridge().doesDisplayNameExists(displayName)) {
            OkPacket packet = new OkPacket(client);
            packet.writePacket(false);
        } else {
            client.setDisplayName(displayName);
            Main.getLoginBridge().updatePlayerStats(client.getSession(), client.getStats());
            OkPacket packet = new OkPacket(client);
            packet.writePacket(true);
        }
    }
}
