package me.eddiep.ghost.test.network.packet;

import me.eddiep.ghost.game.match.world.timeline.EntityDespawnSnapshot;
import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.test.network.TcpUdpClient;
import me.eddiep.ghost.test.network.TcpUdpServer;

import java.io.IOException;

public class DespawnEntityPacket extends Packet<TcpUdpServer, TcpUdpClient> {
    public DespawnEntityPacket(TcpUdpClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(TcpUdpClient client, Object... args) throws IOException {
        if (args.length != 1)
            return;

        EntityDespawnSnapshot e = (EntityDespawnSnapshot)args[0];

        write((byte)0x11)
        .write(e.getID())
        .endTCP();
    }
}
