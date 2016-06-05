package com.boxtrotstudio.ghost.common.network.packet;

import com.boxtrotstudio.ghost.common.network.BasePlayerClient;
import com.boxtrotstudio.ghost.network.packet.Packet;
import com.boxtrotstudio.ghost.common.network.BaseServer;

import java.io.IOException;

public class PingPongPacket extends Packet<BaseServer, BasePlayerClient> {
    public PingPongPacket(BasePlayerClient client, byte[] data) {
        super(client, data);
    }



    @Override
    protected void onHandlePacket(BasePlayerClient client) throws IOException {
        int ping = consume(4).asInt();

        client.endPingTimer(ping);
        writePacket(ping);
    }

    @Override
    protected void onWritePacket(BasePlayerClient client, Object... args) throws IOException {
        client.getServer().sendUdpPacket(
                write((byte)0x09)
                .write((int)args[0])
                .endUDP()
        );
        client.startPingTimer((int)args[0]);
    }
}
