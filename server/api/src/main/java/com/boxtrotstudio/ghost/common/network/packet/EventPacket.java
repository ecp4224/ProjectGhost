package com.boxtrotstudio.ghost.common.network.packet;

import com.boxtrotstudio.ghost.common.network.BasePlayerClient;
import com.boxtrotstudio.ghost.common.network.BaseServer;
import com.boxtrotstudio.ghost.game.match.world.timeline.EventSnapshot;
import com.boxtrotstudio.ghost.network.packet.Packet;

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
                        .write(snapshot.getDirection())
                        .endUDP()
        );
    }
}
