package com.boxtrotstudio.ghost.matchmaking.network.packets;

import com.boxtrotstudio.ghost.matchmaking.network.GameServerClient;
import com.boxtrotstudio.ghost.matchmaking.network.TcpServer;
import com.boxtrotstudio.ghost.network.packet.Packet;

import java.io.IOException;

public class GameServerInfoPacket extends Packet<TcpServer, GameServerClient> {

    @Override
    public void onHandlePacket(GameServerClient client) throws IOException {
        short playerCount = consume(2).asShort();
        short matchCount = consume(2).asShort();
        boolean isFull = consume(1).asBoolean();
        long ticksSkipping = consume(8).asLong();

        if (client.getGameServer() == null)
            return;

        client.getGameServer().updateInfo(playerCount, matchCount, isFull, ticksSkipping);
    }
}
