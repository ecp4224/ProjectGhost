package com.boxtrotstudio.ghost.common.network.packet;

import com.boxtrotstudio.ghost.common.network.BasePlayerClient;
import com.boxtrotstudio.ghost.common.network.BaseServer;
import com.boxtrotstudio.ghost.network.packet.Packet;

import java.io.IOException;

public class PingPongPacket extends Packet<BaseServer, BasePlayerClient> {
    public PingPongPacket() {
        super();
    }
    public PingPongPacket(BasePlayerClient client) {
        super(client);
    }

    @Override
    protected void onHandlePacket(BasePlayerClient client) throws IOException {
        long ping = consume(8).asLong();

        client.onPing();
        new PingPongPacket(client).writePacket(ping);
    }

    @Override
    protected void onWritePacket(BasePlayerClient client, Object... args) throws IOException {
        long toSend = (long) args[0];

        client.getServer().sendUdpPacket(
            write((byte)0x09).
            write(toSend).
            write(new byte[24]).
            endUDP()
        );
    }
}
