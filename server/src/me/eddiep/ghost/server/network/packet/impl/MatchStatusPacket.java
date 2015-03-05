package me.eddiep.ghost.server.network.packet.impl;

import me.eddiep.ghost.server.network.Client;
import me.eddiep.ghost.server.network.packet.Packet;

import java.io.IOException;

public class MatchStatusPacket extends Packet {
    public MatchStatusPacket(Client client) {
        super(client);
    }

    @Override
    protected void onWritePacket(Client client, Object... args) throws IOException {
        if (args.length == 0 || !(args[0] instanceof Boolean))
            return;

        Boolean status = (Boolean) args[0];

        write(0x06)
          .write(status)
          .endTCP();
    }
}
