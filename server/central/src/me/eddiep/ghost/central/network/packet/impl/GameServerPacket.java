package me.eddiep.ghost.central.network.packet.impl;

import me.eddiep.ghost.central.network.Client;
import me.eddiep.ghost.central.network.gameserv.GameServerConnection;
import me.eddiep.ghost.central.network.packet.Packet;

import java.io.IOException;

public class GameServerPacket extends Packet {
    public GameServerPacket(Client client) {
        super(client);
    }

    @Override
    public void onHandlePacket(Client client) throws IOException {
        if (!(client instanceof GameServerConnection)) {
            throw new SecurityException("Arbitrary client made an attempt to send a GameServer Packet !");
        }
    }

    @Override
    public void onWritePacket(Client client, Object... args) throws IOException {
        if (!(client instanceof GameServerConnection)) {
            throw new SecurityException("An attempt was made to send a GameServer Packet to an arbitrary client !");
        }
    }
}
