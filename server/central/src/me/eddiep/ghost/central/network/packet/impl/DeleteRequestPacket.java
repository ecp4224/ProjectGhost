package me.eddiep.ghost.central.network.packet.impl;

import me.eddiep.ghost.central.network.Client;
import me.eddiep.ghost.central.network.packet.Packet;
import me.eddiep.ghost.central.utils.*;

import java.io.IOException;

public class DeleteRequestPacket extends Packet {
    public DeleteRequestPacket(Client client) {
        super(client);
    }

    @Override
    protected void onWritePacket(Client client, Object... args) throws IOException {
        if (args.length != 1)
            return;

        Request request = (Request)args[0];

        write((byte)0x16)
                .write(request.getId())
                .endTCP();
    }
}
