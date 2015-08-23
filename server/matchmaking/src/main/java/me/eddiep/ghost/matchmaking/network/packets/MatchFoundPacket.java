package me.eddiep.ghost.matchmaking.network.packets;

import main.java.matchmaking.network.PlayerClient;
import main.java.matchmaking.network.TcpServer;
import me.eddiep.ghost.network.packet.Packet;

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
