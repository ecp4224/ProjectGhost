package me.eddiep.ghost.server.network.packet.impl;

import me.eddiep.ghost.server.game.Entity;
import me.eddiep.ghost.server.network.Client;
import me.eddiep.ghost.server.network.packet.Packet;

import java.io.IOException;

public class SpawnEntityPacket extends Packet {
    public SpawnEntityPacket(Client client) {
        super(client);
    }

    @Override
    protected void onWritePacket(Client client, Object... args) throws IOException {
        if (args.length != 2)
            return;

        Entity toSpawn = (Entity)args[0];
        byte type = (byte)args[1];

        write((byte)0x10)
                .write(type)
                .write(toSpawn.getID())
                .write((byte)toSpawn.getName().length())
                .write(toSpawn.getName())
                .write(toSpawn.getX())
                .write(toSpawn.getY())
                .endTCP();
    }
}
