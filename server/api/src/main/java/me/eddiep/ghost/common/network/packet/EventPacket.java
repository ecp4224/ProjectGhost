package me.eddiep.ghost.common.network.packet;

import me.eddiep.ghost.common.network.BasePlayerClient;
import me.eddiep.ghost.common.network.BaseServer;
import me.eddiep.ghost.game.match.Event;
import me.eddiep.ghost.game.match.entities.Entity;
import me.eddiep.ghost.game.match.world.timeline.EventSnapshot;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class EventPacket extends Packet<BaseServer, BasePlayerClient> {
    public EventPacket(BasePlayerClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(BasePlayerClient client, Object... args) throws IOException {
        EventSnapshot snapshot = (EventSnapshot)args[0];

        int lastWrite = client.getWriteNumber();

        client.getServer().sendUdpPacket(
                write((byte) 0x40)
                        .write(lastWrite)
                        .write(snapshot.getEventId())
                        .write(snapshot.getCauseId())
                        .endUDP()
        );
    }
}
