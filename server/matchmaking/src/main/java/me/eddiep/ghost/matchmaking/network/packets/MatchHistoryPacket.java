package me.eddiep.ghost.matchmaking.network.packets;

import me.eddiep.ghost.game.stats.MatchHistory;
import main.java.matchmaking.network.GameServerClient;
import main.java.matchmaking.network.TcpServer;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class MatchHistoryPacket extends Packet<TcpServer, GameServerClient> {
    public MatchHistoryPacket(GameServerClient client) {
        super(client);
    }

    @Override
    public void onHandlePacket(GameServerClient client) throws IOException {
        int chunkSize = consume(4).asInt();
        MatchHistory match = consume(chunkSize).as(MatchHistory.class);

        //TODO SAVE IT
    }
}
