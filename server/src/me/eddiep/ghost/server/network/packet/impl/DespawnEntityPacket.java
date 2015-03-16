package me.eddiep.ghost.server.network.packet.impl;

import me.eddiep.ghost.server.game.Entity;
import me.eddiep.ghost.server.network.Client;
import me.eddiep.ghost.server.network.packet.Packet;

import java.io.IOException;

public class DespawnEntityPacket extends Packet {
    public DespawnEntityPacket(Client client) {
        super(client);
    }

    @Override
    protected void onWritePacket(Client client, Object... args) throws IOException {
        if (args.length != 1)
            return;

        Entity e = (Entity)args[0];

        write((byte)0x11)
        .write(e.getID())
        .endTCP();
    }
}
