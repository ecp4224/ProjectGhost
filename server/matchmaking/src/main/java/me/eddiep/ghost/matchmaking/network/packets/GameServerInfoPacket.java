package me.eddiep.ghost.matchmaking.network.packets;

import me.eddiep.ghost.matchmaking.network.GameServerClient;
import me.eddiep.ghost.matchmaking.network.TcpServer;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class GameServerInfoPacket extends Packet<TcpServer, GameServerClient> {
    public GameServerInfoPacket(GameServerClient client) {
        super(client);
    }

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
