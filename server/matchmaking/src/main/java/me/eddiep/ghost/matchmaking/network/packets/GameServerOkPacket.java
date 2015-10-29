package me.eddiep.ghost.matchmaking.network.packets;

import me.eddiep.ghost.matchmaking.network.GameServerClient;
import me.eddiep.ghost.matchmaking.network.TcpServer;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class GameServerOkPacket extends Packet<TcpServer, GameServerClient> {
    public GameServerOkPacket(GameServerClient client) {
        super(client);
    }

    @Override
    public void onHandlePacket(GameServerClient client) throws IOException {
        boolean isOk = consume(1).asBoolean();

        client.setOk(isOk);
    }
}
