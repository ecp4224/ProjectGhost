package com.boxtrotstudio.ghost.matchmaking.network.packets;

import com.boxtrotstudio.ghost.matchmaking.network.PlayerClient;
import com.boxtrotstudio.ghost.network.packet.Packet;
import com.boxtrotstudio.ghost.matchmaking.network.TcpServer;

import java.io.IOException;

public class MatchFoundPacket extends Packet<TcpServer, PlayerClient> {
    public MatchFoundPacket(PlayerClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(PlayerClient client, Object... args) throws IOException{
        if (args.length != 2)
            return;

        float startX = (float)args[0];
        float startY = (float)args[1];

        write((byte)0x02)
                .write(startX)
                .write(startY)
                .endTCP();
    }
}
