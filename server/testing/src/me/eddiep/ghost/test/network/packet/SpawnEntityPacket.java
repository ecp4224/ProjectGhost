package me.eddiep.ghost.test.network.packet;

import me.eddiep.ghost.game.Entity;
import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.test.network.TcpUdpClient;
import me.eddiep.ghost.test.network.TcpUdpServer;

import java.io.IOException;

public class SpawnEntityPacket extends Packet<TcpUdpServer, TcpUdpClient> {
    public SpawnEntityPacket(TcpUdpClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(TcpUdpClient client, Object... args) throws IOException {
        if (args.length != 2)
            return;

        Entity toSpawn = (Entity)args[0];
        byte type = (byte)args[1];

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
