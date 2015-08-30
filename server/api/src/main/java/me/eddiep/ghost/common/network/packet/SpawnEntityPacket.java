package me.eddiep.ghost.common.network.packet;

import me.eddiep.ghost.common.network.BasePlayerClient;
import me.eddiep.ghost.common.network.BaseServer;
import me.eddiep.ghost.game.match.world.timeline.EntitySpawnSnapshot;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class SpawnEntityPacket extends Packet<BaseServer, BasePlayerClient> {
    public SpawnEntityPacket(BasePlayerClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(BasePlayerClient client, Object... args) throws IOException {
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
