package com.boxtrotstudio.ghost.common.network.packet;

import com.boxtrotstudio.ghost.common.network.BasePlayerClient;
import com.boxtrotstudio.ghost.network.packet.Packet;
import com.boxtrotstudio.ghost.common.network.BaseServer;

import java.io.IOException;

public class TcpPingPongPacket extends Packet<BaseServer, BasePlayerClient> {
    public TcpPingPongPacket(BasePlayerClient client) {
        super(client);
    }

    @Override
    protected void onHandlePacket(BasePlayerClient client) throws IOException {
        int ping = consume(4).asInt();

        client.endPingTimer(ping);
        writePacket(ping);
    }

    @Override
    protected void onWritePacket(BasePlayerClient client, Object... args) throws IOException {
        write((byte)0x19)
        .write((int)args[0])
        .endTCP();
    }
}
