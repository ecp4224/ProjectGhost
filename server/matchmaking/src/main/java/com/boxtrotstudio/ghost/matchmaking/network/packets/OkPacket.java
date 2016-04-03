package com.boxtrotstudio.ghost.matchmaking.network.packets;

import com.boxtrotstudio.ghost.matchmaking.network.TcpClient;
import com.boxtrotstudio.ghost.matchmaking.network.TcpServer;
import com.boxtrotstudio.ghost.network.packet.Packet;

import java.io.IOException;

public class OkPacket extends Packet<TcpServer, TcpClient> {
    public OkPacket(TcpClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(TcpClient client, Object... args) throws IOException {
        if (args.length == 0)
            return;

        boolean isOk = (boolean)args[0];

        write((byte)0x01)
                .write(isOk)
                .endTCP();
    }
}
