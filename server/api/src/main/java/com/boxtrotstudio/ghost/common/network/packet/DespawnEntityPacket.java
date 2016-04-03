package com.boxtrotstudio.ghost.common.network.packet;

import com.boxtrotstudio.ghost.common.network.BasePlayerClient;
import com.boxtrotstudio.ghost.game.match.world.timeline.EntityDespawnSnapshot;
import com.boxtrotstudio.ghost.network.packet.Packet;
import com.boxtrotstudio.ghost.common.network.BaseServer;

import java.io.IOException;

public class DespawnEntityPacket extends Packet<BaseServer, BasePlayerClient> {
    public DespawnEntityPacket(BasePlayerClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(BasePlayerClient client, Object... args) throws IOException {
        if (args.length != 1)
            return;

        EntityDespawnSnapshot e = (EntityDespawnSnapshot)args[0];

        write((byte)0x11)
        .write(e.getID())
        .endTCP();
    }
}
