package me.eddiep.ghost.server.network.packet.impl;

import me.eddiep.ghost.server.network.Client;
import me.eddiep.ghost.server.network.packet.Packet;

import java.io.IOException;

public class MatchFoundPacket extends Packet {
    public MatchFoundPacket(Client client) {
        super(client);
    }

    @Override
    protected void onWritePacket(Client client, Object... args) throws IOException{
        if (args.length != 2)
            return;

        float startX = (float)args[0];
        float startY = (float)args[1];

        write((byte)0x02)
                .write(startX)
                .write(startY)
                .endTCP();
    }
}
