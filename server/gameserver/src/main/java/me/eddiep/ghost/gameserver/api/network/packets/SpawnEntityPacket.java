package me.eddiep.ghost.gameserver.api.network.packets;

import me.eddiep.ghost.game.match.world.timeline.EntitySpawnSnapshot;
import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.gameserver.api.network.TcpUdpClient;
import me.eddiep.ghost.gameserver.api.network.TcpUdpServer;

import java.io.IOException;

public class SpawnEntityPacket extends Packet<TcpUdpServer, TcpUdpClient> {
    public SpawnEntityPacket(TcpUdpClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(TcpUdpClient client, Object... args) throws IOException {
        if (args.length != 2)
            return;

        EntitySpawnSnapshot toSpawn = (EntitySpawnSnapshot)args[0];
        short type = (short)args[1];

        write((byte)0x10)
                .write(type)
                .write(toSpawn.getID())
                .write((byte)toSpawn.getName().length())
                .write(toSpawn.getName())
                .write(toSpawn.getX())
                .write(toSpawn.getY())
                .endTCP();
    }
}
