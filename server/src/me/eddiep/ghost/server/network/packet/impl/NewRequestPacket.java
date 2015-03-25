package me.eddiep.ghost.server.network.packet.impl;

import me.eddiep.ghost.server.game.util.Request;
import me.eddiep.ghost.server.network.Client;
import me.eddiep.ghost.server.network.packet.Packet;

import java.io.IOException;

public class NewRequestPacket extends Packet {
    public NewRequestPacket(Client client) {
        super(client);
    }

    @Override
    protected void onWritePacket(Client client, Object... args) throws IOException {
        if (args.length != 1)
            return;

        Request request = (Request)args[0];

        write((byte)0x15)
                .write(request.getId())
                .write(request.getTitle().length())
                .write(request.getDescription().length())
                .write(request.getTitle())
                .write(request.getDescription())
                .endTCP();
    }
}
