package me.eddiep.ghost.central.network.packet.impl;

import me.eddiep.ghost.central.network.Client;
import me.eddiep.ghost.central.network.gameserv.GameServer;
import me.eddiep.ghost.central.network.packet.Packet;

import java.io.IOException;

public class JoinServerPacket extends Packet {
    public JoinServerPacket(Client client) {
        super(client);
    }

    @Override
    public void onWritePacket(Client client, Object... args) throws IOException {
        GameServer toJoin = (GameServer)args[0];

        write((byte)0x21)
                .write(toJoin.getIp().length())
                .write((short)toJoin.getPort())
                .write(toJoin.getIp())
                .endTCP();
    }
}
