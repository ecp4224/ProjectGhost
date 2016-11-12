package com.boxtrotstudio.ghost.common.network.packet;

import com.boxtrotstudio.ghost.common.network.BasePlayerClient;
import com.boxtrotstudio.ghost.common.network.BaseServer;
import com.boxtrotstudio.ghost.game.match.world.timeline.EntitySpawnSnapshot;
import com.boxtrotstudio.ghost.network.packet.Packet;

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
                .write(toSpawn.getName().length())
                .write(toSpawn.getName())
                .write(toSpawn.getX())
                .write(toSpawn.getY())
                .write(toSpawn.getRotation())
                .write(toSpawn.getWidth())
                .write(toSpawn.getHeight())
                .write(toSpawn.hasLighting())
                .endTCP();
    }
}
