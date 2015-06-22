package me.eddiep.ghost.gameserver.packets;

import me.eddiep.ghost.gameserver.TcpUdpClient;
import me.eddiep.ghost.gameserver.TcpUdpServer;
import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.game.Entity;

import java.io.IOException;

public class DespawnEntityPacket extends Packet<TcpUdpServer, TcpUdpClient> {
    public DespawnEntityPacket(TcpUdpClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(TcpUdpClient client, Object... args) throws IOException {
        if (args.length != 1)
            return;

        Entity e = (Entity)args[0];

        write((byte)0x11)
        .write(e.getID())
        .endTCP();
    }
}
