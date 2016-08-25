package com.boxtrotstudio.ghost.matchmaking.network.packets;

import com.boxtrotstudio.ghost.matchmaking.network.GameServerClient;
import com.boxtrotstudio.ghost.network.packet.Packet;
import com.boxtrotstudio.ghost.matchmaking.network.TcpServer;

import java.io.IOException;

public class GameServerOkPacket extends Packet<TcpServer, GameServerClient> {

    @Override
    public void onHandlePacket(GameServerClient client) throws IOException {
        boolean isOk = consume(1).asBoolean();

        client.setOk(isOk);
    }
}
