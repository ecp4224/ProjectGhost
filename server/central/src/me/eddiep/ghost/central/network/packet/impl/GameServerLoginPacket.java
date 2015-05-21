package me.eddiep.ghost.central.network.packet.impl;

import me.eddiep.ghost.central.network.Client;

import java.io.IOException;

public class GameServerLoginPacket extends GameServerPacket {

    public GameServerLoginPacket(Client client) {
        super(client);
    }

    @Override
    public void onWritePacket(Client client, Object... args) throws IOException {
        super.onWritePacket(client, args);

        String secret = (String) args[0];
        if (secret.length() > 36)
            secret = secret.substring(0, 36);

        write((byte)0x00)
                .write(secret)
                .endTCP();
    }
}
