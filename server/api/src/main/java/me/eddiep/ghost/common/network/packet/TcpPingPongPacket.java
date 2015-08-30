package me.eddiep.ghost.common.network.packet;

import me.eddiep.ghost.common.network.BasePlayerClient;
import me.eddiep.ghost.common.network.BaseServer;
import me.eddiep.ghost.network.packet.Packet;

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
