package me.eddiep.ghost.common.network.packet;

import me.eddiep.ghost.common.network.BasePlayerClient;
import me.eddiep.ghost.common.network.BaseServer;
import me.eddiep.ghost.game.match.world.timeline.EntityDespawnSnapshot;
import me.eddiep.ghost.network.packet.Packet;

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
