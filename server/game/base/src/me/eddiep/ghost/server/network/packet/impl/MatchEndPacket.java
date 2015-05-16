package me.eddiep.ghost.server.network.packet.impl;

import me.eddiep.ghost.server.network.Client;
import me.eddiep.ghost.server.network.packet.Packet;

import java.io.IOException;

public class MatchEndPacket extends Packet {
    public MatchEndPacket(Client client) {
        super(client);
    }

    @Override
    protected void onWritePacket(Client client, Object... args) throws IOException {
        if (args.length != 2)
            return;

        Boolean winrar = (Boolean) args[0];
        Long matchId = (Long)args[1];

        write((byte)0x07)
                .write(winrar)
                .write(matchId)
                .endTCP();
    }
}
